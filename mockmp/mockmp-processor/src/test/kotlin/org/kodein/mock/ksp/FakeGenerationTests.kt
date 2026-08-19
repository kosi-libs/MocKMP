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
}
