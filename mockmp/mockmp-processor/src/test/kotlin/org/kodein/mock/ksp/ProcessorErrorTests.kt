package org.kodein.mock.ksp

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * The processor's compile-time diagnostics, driven by compiling little source files that stand in for
 * user code. Every assertion is on a *substring* of the message — the clause that distinguishes that
 * diagnostic from the others — so wording can be improved without churn here, while a diagnostic that
 * stops being produced, or starts being produced for the wrong reason, still fails.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class ProcessorErrorTests {

    // The exception the processor aborts a round with. Named as a string because it is private
    // to MocKMPProcessor; its presence in the output is what distinguishes a thrown error from a
    // logged one.
    private val ProcessingErrorName = "MocKMPProcessor\$ProcessingError"

    /** The compilation itself, for tests that read what was generated rather than only what was reported. */
    private fun compilation(
        source: String,
        options: Map<String, String> = emptyMap(),
    ): KotlinCompilation =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Fixture.kt", source))
            inheritClassPath = true
            // Matches mockmp-runtime's own jvmToolchain(11) (mockmp-runtime/build.gradle.kts):
            // inheritClassPath puts its compiled classes on this compilation's classpath, and a
            // fixture calling one of its reified inline functions (mockFunctionN & co.) has that
            // inlined into its own bytecode — which fails against kctfork's lower default target.
            jvmTarget = "11"
            useKsp2()
            configureKsp {
                symbolProcessorProviders += MocKMPProcessorProvider()
                processorOptions.putAll(options)
            }
        }

    private fun compile(
        source: String,
        options: Map<String, String> = emptyMap(),
    ): JvmCompilationResult = compilation(source, options).compile()

    @Test
    fun mockOnNonInterface() {
        val result = compile(
            """
            import org.kodein.mock.Mock

            class NotAnInterface

            class Tests {
                @Mock lateinit var notAnInterface: NotAnInterface
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate mock for non interface")
    }

    @Test
    fun fakeOnImmutableProperty() {
        val result = compile(
            """
            import org.kodein.mock.Fake

            data class User(val name: String)

            class Tests {
                @Fake val user: User = User("")
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "is immutable but is annotated with @Fake")
    }

    // `x`'s type resolves to a bare KSTypeParameter here — @Fake reads a property's declared type
    // with no substitution (lookUpFields), unlike every other route into addFake, which either
    // resolves generics against a concrete instantiation or can't name a type parameter at all
    // (a KClass literal can't express an unbound T). This is a hard failure regardless of whether
    // the property is ever read: there's no concrete type to name a fakeXxx() function after.
    @Test
    fun fakeOnAPropertyTypedAsItsOwnClassTypeParameter() {
        val result = compile(
            """
            import org.kodein.mock.Fake

            class Container<T> {
                @Fake lateinit var x: T
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "it is a type parameter, which has no concrete type to construct")
    }

    // `addFake` now resolves [type] the same way `valueTypeToFake` resolves a nested occurrence
    // before registering it — so a nullable `@Fake`/`@UsesFakes` target is recognized as needing no
    // fake at all, exactly as a nullable constructor parameter or implemented member already was.
    // Without that, `String?` used to get registered as its own fake target, generating
    // `fakekotlin_NulString(): String? = String()`; that phantom entry polluted
    // generatePlaceholderAccessor's declaration-keyed fakesByDecl (colliding with the ordinary,
    // non-null `String::class -> ""` builtin branch) and broke the whole compilation on a
    // return-type mismatch, and separately, addFakeInjection (via fakeInitializerOf, which has no
    // nullability check) faked the property as `""` instead of `null`. addFakeInjection now goes
    // through fakeValueOf instead, which fixes both: see testFakedProperties for the nullable-value
    // half, verified end-to-end in tests-mp-junit4.
    @Test
    fun fakeOnANullableVarIsFakedAsNull() {
        val compilation = compilation(
            """
            import org.kodein.mock.Fake

            class Tests {
                @Fake var x: String? = "unset"
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val injector = compilation.workingDir.walkTopDown().first { it.name == "Tests_injectMocks.kt" }.readText()
        assertContains(injector, "receiver.x = null")
    }

    // A transitive route into the same failure, via nothing more exotic-looking than
    // `@UsesFakes(Multi::class)`: `Multi::class` itself registers fine (it's a real class) — the
    // type parameter only surfaces one level down, expanding Multi's constructor. `U`'s bound is
    // `T`, a *sibling* type parameter of the same class; for a star-projected `Multi<*, *>`,
    // substituteTypeParameters resolves `U` to `decl.boundType()` — `T` itself — without ever
    // re-substituting T against vType's own arguments (constructorParamTypeToFake:906,
    // substituteTypeParameters:858). `T : Any` is required to reach *this* message specifically: an
    // unbounded `T` resolves with KSP-reported `nullability == NULLABLE` despite `isMarkedNullable
    // == false` (its effective bound is `Any?`), which fakeValueOf/valueTypeToFake read as
    // "nullable" and fake as a plain `null` instead — see the sibling
    // fakeTransitivelyRequiringANullableSiblingBoundedTypeParameter test below.
    @Test
    fun fakeTransitivelyRequiringASiblingBoundedTypeParameter() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            class Multi<T : Any, U : T>(val value: U)

            @UsesFakes(Multi::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for Multi.T because it is a type parameter, which has no concrete type to construct.")
        assertContains(result.messages, "Required by: Multi<*, *>(U)")
    }

    // The unbounded counterpart of the test above: with no explicit bound on T, U resolves to a
    // *nullable* T (see that test's comment for why), so this compiles fine and fakes `value` as a
    // plain `null` — even though `U` is declared as a non-nullable type parameter. This mirrors the
    // already-documented "an undeclared bound is Any?" convention (see NullGenData<T> in
    // faking.adoc's Generics section) one level of indirection deeper: U's own bound is T, and T's
    // bound is undeclared, so U's effective bound is Any? too. Pinned down here as current
    // behavior, not (yet) asserted to be correct or incorrect.
    @Test
    fun fakeTransitivelyRequiringANullableSiblingBoundedTypeParameter() {
        val compilation = compilation(
            """
            import org.kodein.mock.UsesFakes

            class Multi<T, U : T>(val value: U)

            @UsesFakes(Multi::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakeMultiXSTAR_STARX.kt" }.readText()
        assertContains(source, "Multi(value = null)")
    }

    @Test
    fun mockOnNonProperty() {
        val result = compile(
            """
            import org.kodein.mock.Mock

            interface Api

            class Tests {
                @Mock fun notAProperty() {}
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "is not a property nor a property setter")
    }

    @Test
    fun mockOnPropertyOutsideAClass() {
        val result = compile(
            """
            import org.kodein.mock.Mock

            interface Api

            @Mock lateinit var topLevelApi: Api
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "as it is not inside a class")
    }

    @Test
    fun fakeProviderNotTopLevel() {
        val result = compile(
            """
            import org.kodein.mock.FakeProvider

            data class User(val name: String)

            class Providers {
                @FakeProvider fun provideUser() = User("")
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Only top-level functions can be annotated with @FakeProvider")
    }

    @Test
    fun duplicateFakeProvider() {
        val result = compile(
            """
            import org.kodein.mock.FakeProvider

            data class User(val name: String)

            @FakeProvider fun provideUser() = User("")
            @FakeProvider fun provideOtherUser() = User("other")
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Only one @FakeProvider function must exist for this type")
    }

    @Test
    fun fakeProviderReturningANonClassType() {
        val result = compile(
            """
            import org.kodein.mock.FakeProvider

            @FakeProvider fun <T> provideAnything(): T = TODO()
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "@FakeProvider functions must return class types.")
    }

    // The wording shared by the compile-time error and the runtime stub (unfakeableMessage): a concrete
    // reason, then the @FakeProvider suggestion. A private constructor is what's left once interfaces,
    // abstract classes, objects and annotation classes are all fakeable.
    @Test
    fun unfakeableTypeStatesAReasonAndSuggestsFakeProvider() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            class NotConstructible private constructor()

            @UsesFakes(NotConstructible::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for NotConstructible")
        assertContains(result.messages, "it has no public constructor")
        assertContains(result.messages, "register a top-level @FakeProvider function")
        // Nothing required it: the user asked for this type by name, so there is no chain to report.
        assertFalse(
            "Required by:" in result.messages,
            "a directly requested type should not be explained by a path:\n${result.messages}",
        )
    }

    // A fake is usually reached transitively, so naming only the type that failed leaves the user with
    // nothing of theirs to look at. Every hop is reported: an interface property, an interface function
    // return, then a constructor parameter — the three ways one type comes to require another.
    @Test
    fun unfakeableTypeReportsWhatRequiredIt() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            class TCPLayer private constructor()

            class Network(val tcp: TCPLayer, val timeout: Int)

            interface Connection {
                fun connect(creds: String): Network
            }

            interface Database {
                val con: Connection
            }

            @UsesFakes(Database::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for TCPLayer")
        assertContains(
            result.messages,
            "Required by: Database.con: Connection -> Connection.connect(String): Network -> Network(TCPLayer, Int)",
        )
    }

    // The other half of the same wording: a type reached only from a *mocked* interface's parameter
    // types is a Placeholder, always implicit, so it does not fail the build — it gets a stub that
    // throws if it is ever used. That stub is where the path matters most, since nothing about such a
    // type appears in the user's code at all. Asserted on the generated source, as nothing is
    // reported at compile time.
    @Test
    fun anImplicitlyRequiredUnfakeableTypeCarriesItsPathIntoTheGeneratedStub() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesMocks

            class TCPLayer private constructor()

            class Network(val tcp: TCPLayer, val timeout: Int)

            interface Service {
                fun net(n: Network)
            }

            @UsesMocks(Service::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val stub = compilation.workingDir.walkTopDown().first { it.name == "placeholderfixture_TCPLayer.kt" }.readText()
        assertContains(stub, "Cannot generate a fake for fixture.TCPLayer")
        assertContains(stub, "Required by: Service.net(Network): Unit -> Network(TCPLayer, Int)")
    }

    // A function type never needs a named fakeXxx() of its own: its value is an inline no-op lambda
    // (see fakeValueOf), exactly as it already was for one nested inside another fake. addFake now
    // unwraps it to its return type instead of registering (and, formerly, rejecting) the function
    // type itself — addFakeInjection's switch to fakeValueOf is what actually emits the lambda here.
    @Test
    fun fakeOnAFunctionTypedPropertyGeneratesANoOpLambda() {
        val compilation = compilation(
            """
            import org.kodein.mock.Fake

            class Tests {
                @Fake lateinit var callback: (String) -> Int
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val injector = compilation.workingDir.walkTopDown().first { it.name == "Tests_injectMocks.kt" }.readText()
        assertContains(injector, "receiver.callback = { _, -> 0 }")
    }

    // The one function-type shape that still can't be unwrapped to a return type: a raw,
    // unparameterized KClass literal reference, only reachable this way — a genuine declared
    // (String) -> Int always carries its full signature. Without addFake's guard, this would fall
    // through as an ordinary INTERFACE and hit the Kotlin/JS restriction the whole check exists to
    // avoid (a class can't declare a function interface as a supertype).
    @Test
    fun fakeOnARawFunctionInterfaceReferenceStillPointsAtMock() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            @UsesFakes(Function1::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for the function type")
        assertContains(result.messages, "Please use @Mock instead")
    }

    // The suggestion every one of the tests above points to actually works: a plain function type
    // is mockable — addMock (unlike addFake) special-cases it deliberately (`:isAnyFunctionType ->
    // return`), skipping the "implement it" path entirely, and addMockInjection builds the
    // assignment from mockFunctionN/mockSuspendFunctionN instead. Runtime behaviour (stubbing,
    // verifying) is covered end-to-end in tests-mp-junit4's InjectionTests and FunctionArityTests;
    // this is the compile-time half.
    @Test
    fun mockOnAFunctionTypedPropertyCompiles() {
        val result = compile(
            """
            import org.kodein.mock.Mock

            class Tests {
                @Mock lateinit var callback: (String) -> Int
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
    }

    // An interface is faked by generating a class that implements it: no-op functions, faked
    // properties. This is the compile-time half — that no diagnostic is produced for what used to be
    // an error; InterfaceFakeTests in tests-mp-junit4 asserts the generated behaviour.
    @Test
    fun interfacesAndAbstractClassesAreFakeable() {
        val result = compile(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            interface Api {
                val name: String
                fun log(message: String)
                fun count(): Int
            }

            abstract class AbsApi(val id: Int) : Api

            @UsesFakes(Api::class, AbsApi::class)
            class Tests
            """,
            // The accessors are generated as `actual` declarations by default, which this
            // single-module compilation is not set up for — and the point here is the fake, not them.
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
    }

    // `Nothing` has no values, so a member typed `Nothing` cannot hold or return one — see
    // MocKMPProcessor.builtins. This used to fail the compilation regardless of whether the type was
    // reached by faking (fail()/impossible below) or only implicitly, through a mocked interface's own
    // signature (seedImplicitPlaceholder): either path queued `kotlin.Nothing` for generation, and the
    // resulting `fakes.kt` declared `private val type_kotlin_Nothing: KType = typeOf<Nothing>()`, which
    // the Kotlin compiler rejects ("Cannot use 'Nothing' as reified type parameter"). Mocking and faking
    // the same interface here exercises both paths in one compilation.
    @Test
    fun nothingTypedMembersAreFakeable() {
        val result = compile(
            """
            package fixture

            import org.kodein.mock.UsesFakes
            import org.kodein.mock.UsesMocks

            interface Service {
                val impossible: Nothing
                fun fail(): Nothing
            }

            @UsesFakes(Service::class)
            @UsesMocks(Service::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
    }

    // The two halves of the throwErrors option, over one bad input. Both fail the compilation and both
    // log the reason; what the option adds is that the ProcessingError *escapes* the processor, so KSP
    // reports its own "Error occurred in KSP" on top and the stack trace reaches the output. The pair
    // below is written around exactly that difference — asserting the absence of the trace in one and
    // its presence in the other — because an assertion both modes satisfy would not test the option.
    private val unmockableSource =
        """
        import org.kodein.mock.Mock

        class NotAnInterface

        class Tests {
            @Mock lateinit var notAnInterface: NotAnInterface
        }
        """

    @Test
    fun errorsAreLoggedByDefault() {
        val result = compile(unmockableSource)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "MocKMP: ")
        assertContains(result.messages, "Cannot generate mock for non interface")
        assertFalse(
            ProcessingErrorName in result.messages,
            "the round should have been aborted quietly, but the error escaped:\n${result.messages}",
        )
    }

    @Test
    fun errorsAreThrownWhenAsked() {
        val result = compile(
            unmockableSource,
            options = mapOf("org.kodein.mock.errors" to "throw"),
        )
        assertNotEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate mock for non interface")
        assertContains(result.messages, ProcessingErrorName)
    }

    // A function *returning* a function can't be unwrapped to a fakeable return type either: its
    // return type is itself a function type, and registering that would only "succeed" by
    // generating exactly the shape this check exists to prevent — a class declaring Function1 &
    // co. as a supertype, which Kotlin/JS forbids (fakeValueOf's inline lambda generation only
    // inlines one level itself, calling fakeInitializerOf directly on the return type). Rejected the
    // same way a plain function type always was, regardless of how deep the nesting goes.
    @Test
    fun fakeOnAFunctionReturningAFunctionPointsAtMock() {
        val result = compile(
            """
            import org.kodein.mock.Fake

            class Tests {
                @Fake lateinit var cb: (String) -> (Int) -> Boolean
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for the function type")
        assertContains(result.messages, "Please use @Mock instead")
    }

    // An object is already its own single instance: generateFakeFunction already had a working
    // ClassKind.OBJECT branch (a bare reference to the singleton), previously reachable only via a
    // sealed permitted subclass — addFake's classKind check rejected a directly-requested object
    // before that branch was ever consulted.
    @Test
    fun fakeOnAnObjectReturnsTheSingleton() {
        val compilation = compilation(
            """
            import org.kodein.mock.UsesFakes

            object Foo {
                val x: Int = 42
            }

            @UsesFakes(Foo::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val fake = compilation.workingDir.walkTopDown().first { it.name == "fakeFoo.kt" }.readText()
        assertContains(fake, "fun fakeFoo(): Foo = Foo")
    }

    // An annotation class's constructor is called exactly like any other class's — generateFakeFunction
    // had no branch for ClassKind.ANNOTATION_CLASS at all, so even removing addFake's rejection would
    // only have swapped which check reported "cannot be instantiated".
    @Test
    fun fakeOnAnAnnotationClassCallsItsConstructor() {
        val compilation = compilation(
            """
            import org.kodein.mock.UsesFakes

            annotation class Foo(val name: String, val count: Int)

            @UsesFakes(Foo::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val fake = compilation.workingDir.walkTopDown().first { it.name == "fakeFoo.kt" }.readText()
        assertContains(fake, "fun fakeFoo(): Foo = Foo(name = \"\", count = 0)")
    }

    // KClass<T>'s value depends on which T is actually being faked — String::class for Foo<String>,
    // not a context-free literal the way an empty collection is valid for any element type.
    // fakeInitializerOf derives it from the resolved type argument instead of reading a fixed value
    // off builtins, which is why KClass/Class need special-casing there and not just a map entry.
    @Test
    fun fakeConstructorParameterOfKClassTypeUsesTheActualTypeArgument() {
        val compilation = compilation(
            """
            import org.kodein.mock.UsesFakes
            import kotlin.reflect.KClass

            annotation class Foo<T : Any>(val cls: KClass<T>)

            class Bar(val foo: Foo<String>)

            @UsesFakes(Bar::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val fake = compilation.workingDir.walkTopDown()
            .first { it.name.startsWith("fakeFoo") && it.name.endsWith(".kt") }
            .readText()
        assertContains(fake, "cls = String::class")
    }

    // java.lang.Class<T> is builtins' other context-dependent entry, alongside KClass<T> above —
    // same special-casing in fakeInitializerOf, same derivation from the resolved type argument.
    @Test
    fun fakeConstructorParameterOfJavaClassTypeUsesTheActualTypeArgument() {
        val compilation = compilation(
            """
            import org.kodein.mock.UsesFakes

            class Foo<T : Any>(val cls: Class<T>)

            class Bar(val foo: Foo<String>)

            @UsesFakes(Bar::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val fake = compilation.workingDir.walkTopDown()
            .first { it.name.startsWith("fakeFoo") && it.name.endsWith(".kt") }
            .readText()
        assertContains(fake, "cls = String::class")
    }
}
