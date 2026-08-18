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

    private fun compile(
        source: String,
        options: Map<String, String> = emptyMap(),
    ): JvmCompilationResult =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Fixture.kt", source))
            inheritClassPath = true
            useKsp2()
            configureKsp {
                symbolProcessorProviders += MocKMPProcessorProvider()
                processorOptions.putAll(options)
            }
        }.compile()

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
    // reason, then the @FakeProvider suggestion. An annotation class can be neither constructed nor
    // implemented, which is what is left once interfaces and abstract classes are fakeable.
    @Test
    fun unfakeableTypeStatesAReasonAndSuggestsFakeProvider() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            annotation class NotConstructible

            @UsesFakes(NotConstructible::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for NotConstructible")
        assertContains(result.messages, "annotation classes cannot be instantiated")
        assertContains(result.messages, "register a top-level @FakeProvider function")
    }

    // A function type resolves to an interface declaration (kotlin.Function1 & co.), so it would
    // otherwise take the "fake it by implementing it" path — which Kotlin/JS forbids for exactly
    // these types. @Mock is the supported way to get a callable stand-in for one.
    @Test
    fun fakeOnAFunctionTypedPropertyPointsAtMock() {
        val result = compile(
            """
            import org.kodein.mock.Fake

            class Tests {
                @Fake lateinit var callback: (String) -> Int
            }
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for the function type")
        assertContains(result.messages, "Please use @Mock instead")
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
}
