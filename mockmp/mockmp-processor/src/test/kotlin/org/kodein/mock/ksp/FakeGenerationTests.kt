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
}
