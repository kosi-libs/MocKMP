package org.kodein.mock.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Asserts the exact shape `fakedProperty` generates for a faked interface's properties, rather than
 * just that the compilation succeeds: which properties are wrapped `by LazyFake { ... }`, and which
 * keep a plain literal (or, for `Nothing`, a throwing getter). [ProcessorErrorTests] covers
 * diagnostics; this covers generated code shape, so it reads the source KSP actually wrote.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class FakeGenerationTests {

    private fun compilation(
        source: String,
        options: Map<String, String> = emptyMap(),
    ): KotlinCompilation =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Fixture.kt", source))
            inheritClassPath = true
            useKsp2()
            configureKsp {
                symbolProcessorProviders += MocKMPProcessorProvider()
                processorOptions.putAll(options)
            }
        }

    /** The trimmed generated line declaring property [name], e.g. `"override val name: String = ..."`. */
    private fun String.propertyLine(name: String): String =
        lineSequence().map { it.trim() }.first { it.startsWith("override val $name:") || it.startsWith("override var $name:") }

    /**
     * The generated `FakeXxx` implementation class is a detail of the `fakeXxx()` function that
     * builds it — it must stay `private` to its file, so it can never collide with a user
     * declaration in the faked type's own package, and it must be declared *after* that function, so
     * the file reads as "here is how to get a fake" before "here is how it's implemented".
     */
    @Test
    fun fakeImplementationClassIsPrivateAndDeclaredAfterItsFakeFunction() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            interface Api {
                val name: String
                fun greet(): String
            }

            abstract class AbsApi(val id: Int) {
                abstract val name: String
            }

            @UsesFakes(Api::class, AbsApi::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        listOf("fakefixture_Api.kt" to "FakeApi", "fakefixture_AbsApi.kt" to "FakeAbsApi").forEach { (fileName, className) ->
            val source = generated.first { it.name == fileName }.readText()
            val funLine = source.lineSequence().map { it.trim() }.indexOfFirst { it.startsWith("internal fun fake") }
            val classLine = source.lineSequence().map { it.trim() }.indexOfFirst { it.startsWith("private class $className") }

            assertTrue(funLine >= 0, "Expected a fakeXxx() function in $fileName:\n$source")
            assertTrue(classLine >= 0, "Expected `private class $className` in $fileName:\n$source")
            assertTrue(funLine < classLine, "Expected the fakeXxx() function to be declared before $className in $fileName:\n$source")

            assertFalse("internal class $className" in source, "Did not expect $className to be internal:\n$source")
            assertFalse("public class $className" in source, "Did not expect $className to be public:\n$source")
        }
    }

    /**
     * Companion of the test above, with `org.kodein.mock.visibility=public`: the generated
     * `fakeXxx()` function becomes `public`, but the `FakeXxx` class it returns stays `private` — a
     * `private` type returned by a wider-visibility function is legal in Kotlin only because the
     * function's declared return type is the faked interface itself, never the private class (see
     * addFakeImplementation's KDoc). This must still compile.
     */
    @Test
    fun fakeImplementationClassStaysPrivateEvenWhenTheFakeFunctionIsPublic() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            interface Api {
                val name: String
            }

            @UsesFakes(Api::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false", "org.kodein.mock.visibility" to "public"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_Api.kt" }.readText()
        assertTrue("public fun fakefixture_Api" in source, "Expected the fake function to be public:\n$source")
        assertTrue("private class FakeApi" in source, "Expected the fake class to stay private:\n$source")
    }

    @Test
    fun onlyReferenceTypedPropertiesAreBackedByLazyFake() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            data class Data(val value: String)

            interface Api {
                val int: Int
                val string: String
                val list: List<String>
                val map: Map<String, Int>
                val optionalRef: Data?
                val callback: (String) -> Int
                val impossible: Nothing
                val ref: Data
                var mutableRef: Data
            }

            @UsesFakes(Api::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_Api.kt" }.readText()

        // Primitive (Int) and special/builtin (String, List, Map) types, a nullable reference type
        // (whose value is `null`, nothing to defer), a function type (a lambda literal, itself
        // already only evaluated when called) and Nothing (its own throwing-getter shape) — none of
        // these hold a nested fake call, so none of them is deferred through LazyFake.
        listOf("int", "string", "list", "map", "optionalRef", "callback", "impossible").forEach { name ->
            val line = source.propertyLine(name)
            assertFalse("LazyFake" in line, "Expected `$name` not to be backed by LazyFake:\n$line")
        }

        // `Data` has no builtin representation, so its value is a nested `fakeXxx()` call — both the
        // `val` and the `var` are backed by LazyFake, deferring that call to first read.
        assertEquals("override val ref: Data by LazyFake { fakefixture_Data() }", source.propertyLine("ref"))
        assertEquals("override var mutableRef: Data by LazyFake { fakefixture_Data() }", source.propertyLine("mutableRef"))

        // Sanity check that the two matches above are the *only* uses of LazyFake in the file: one
        // import plus one delegate per reference-typed property, nothing attributed to the wrong one.
        assertEquals(3, Regex("LazyFake").findAll(source).count())
        assertTrue(source.contains("import org.kodein.mock.LazyFake"))
    }

    @Test
    fun coroutineTypesAreFakedAsBuiltins() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.channels.Channel

            data class Data(val value: String)

            interface Api {
                val flow: Flow<String>
                val job: Job
                val channel: Channel<String>
                val state: StateFlow<Data>
            }

            @UsesFakes(Api::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        val source = generated.first { it.name == "fakefixture_Api.kt" }.readText()

        // Flow/Job/Channel are literal builtin calls, same category as List/Map — no nested
        // fakeXxx() call, so no LazyFake wrapper.
        listOf("flow", "job", "channel").forEach { name ->
            val line = source.propertyLine(name)
            assertFalse("LazyFake" in line, "Expected `$name` not to be backed by LazyFake:\n$line")
        }
        assertEquals("override val flow: Flow<String> = emptyFlow()", source.propertyLine("flow"))
        // Fully qualified: `Job` (the property's own type, already imported) and the top-level
        // `Job(...)` factory function share one qualified name, so KotlinPoet backs off importing
        // the function under the name its interface already claimed and spells it out instead —
        // still an unambiguous, compiling call to the same factory. Same reasoning for Channel.
        assertEquals("override val job: Job = kotlinx.coroutines.Job()", source.propertyLine("job"))
        assertEquals("override val channel: Channel<String> = kotlinx.coroutines.channels.Channel()", source.propertyLine("channel"))

        // StateFlow<Data> embeds a faked Data, so it needs the same LazyFake deferral a nested
        // fakeXxx() call always does, and a fakeData() function must actually be generated for it.
        assertEquals("override val state: StateFlow<Data> by LazyFake { MutableStateFlow(fakefixture_Data()) }", source.propertyLine("state"))
        assertTrue(generated.any { it.name == "fakefixture_Data.kt" }, "Expected a fakeData() function to be generated for StateFlow<Data>'s type argument")

        // None of Flow/Job/Channel/StateFlow is faked by generating an implementation class: no
        // FakeFlow/FakeJob/FakeChannel/FakeStateFlow file should exist.
        listOf("Flow", "Job", "Channel", "StateFlow").forEach { name ->
            assertFalse(generated.any { it.name.startsWith("Fake$name") }, "Did not expect a generated Fake$name implementation")
        }
    }

    @Test
    fun fakeProviderOverridesBuiltin() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes
            import org.kodein.mock.FakeProvider

            interface Api {
                val list: List<String>
            }

            @FakeProvider
            internal fun provideFakeStringList() = listOf("a", "b")

            @UsesFakes(Api::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        // The provider wins over the emptyList() builtin — and, being a function call like any other
        // nested fake, still needs the same LazyFake deferral (see needsLazyFake).
        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_Api.kt" }.readText()
        assertEquals("override val list: List<String> by LazyFake { provideFakeStringList() }", source.propertyLine("list"))
    }

    /**
     * Regression test: `isAny<T>()` resolves its placeholder through the erased `T::class`, which
     * cannot tell "T was Suit" apart from "T was Suit?" — so a type reachable only as a nullable
     * parameter of a mocked interface still needs a real placeholder, exactly as a non-nullable
     * reference to it would (see seedImplicitPlaceholder).
     */
    @Test
    fun nullableOnlyReferencedEnumStillGetsAPlaceholder() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

            interface CardGame {
                fun play(suit: Suit?)
            }

            class Tests {
                @Mock
                lateinit var game: CardGame
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        assertTrue(
            generated.any { it.name == "fakefixture_Suit.kt" },
            "Expected a fakeSuit() function to be generated even though Suit is only ever referenced nullably",
        )
        val placeholders = generated.first { it.name == "placeholders.kt" }.readText()
        assertTrue("Suit::class ->" in placeholders, "Expected a providePlaceholder branch for Suit:\n$placeholders")
    }

    /**
     * The mirror of [nullableOnlyReferencedEnumStillGetsAPlaceholder]: seedImplicitPlaceholder's fix
     * is scoped to mocked-interface members only. A type reached only through a nullable `@Fake`
     * constructor parameter is unaffected — it still fakes as a plain `null`, with no `fakeXxx()`
     * function generated for it at all, exactly as before this fix (see valueTypeToFake, addFake).
     */
    @Test
    fun nullableOnlyReferencedEnumInAFakeIsStillJustNullWithNoGeneratedFunction() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

            data class Hand(val suit: Suit?)

            @UsesFakes(Hand::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        val source = generated.first { it.name == "fakefixture_Hand.kt" }.readText()
        assertTrue("suit = null" in source, "Expected suit to be faked as a plain null:\n$source")
        assertFalse(
            generated.any { it.name == "fakefixture_Suit.kt" },
            "Did not expect a fakeSuit() function: Suit is never referenced non-nullably here",
        )
    }
}
