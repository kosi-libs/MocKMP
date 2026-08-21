package org.kodein.mock.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Asserts that a generated `MockXxx`/`FakeXxx`/`PlaceholderXxx` implementing (or extending) a type
 * annotated `kotlin.SubclassOptInRequired` — e.g. kotlinx.serialization's `SerialDescriptor`,
 * annotated `@SubclassOptInRequired(SealedSerializationApi::class)` — carries a matching `@OptIn` on
 * its own declaration, or the generated file fails to compile with "This class or interface requires
 * opt-in to be implemented." `@SubclassOptInRequired`/`@OptIn` are plain Kotlin stdlib (`kotlin`
 * package), so the fixture below needs no extra test dependency.
 *
 * `KotlinCompilation.ExitCode.OK` is the real assertion here: without
 * [MocKMPProcessor.addSubclassOptInAnnotation], every one of these fails to compile exactly like the
 * real-world `SerialDescriptor` case did. The source-text checks on top of that pin down *why* it
 * compiles, not just that it does.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class SubclassOptInRequiredTests {

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

    private val restrictedFixture =
        """
        @RequiresOptIn
        annotation class ExperimentalMarker

        @SubclassOptInRequired(ExperimentalMarker::class)
        interface Restricted {
            fun doThing()
        }

        @SubclassOptInRequired(ExperimentalMarker::class)
        abstract class RestrictedAbs {
            abstract fun doThing()
        }
        """.trimIndent()

    @Test
    fun mockedInterfaceOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesMocks

            $restrictedFixture

            @UsesMocks(Restricted::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "MockRestricted.kt" }.readText()
        assertTrue(
            "@OptIn(ExperimentalMarker::class)" in source,
            "Expected MockRestricted to opt in on ExperimentalMarker's behalf:\n$source",
        )
    }

    @Test
    fun mockedAbstractClassOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesMocks

            $restrictedFixture

            @UsesMocks(RestrictedAbs::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "MockRestrictedAbs.kt" }.readText()
        assertTrue(
            "@OptIn(ExperimentalMarker::class)" in source,
            "Expected MockRestrictedAbs to opt in on ExperimentalMarker's behalf:\n$source",
        )
    }

    @Test
    fun fakedInterfaceOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            $restrictedFixture

            @UsesFakes(Restricted::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_Restricted.kt" }.readText()
        assertTrue(
            "@OptIn(ExperimentalMarker::class)" in source,
            "Expected FakeRestricted to opt in on ExperimentalMarker's behalf:\n$source",
        )
    }

    /**
     * `Restricted` reached only transitively, as a mocked interface's own property type — the case
     * that actually motivated this feature (kotlinx.serialization's `KSerializer.descriptor:
     * SerialDescriptor`) — so it gets a Placeholder, not a Mock or a Fake.
     */
    @Test
    fun placeholderInterfaceOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            $restrictedFixture

            interface Service {
                val restricted: Restricted
            }

            class Tests {
                @Mock
                lateinit var service: Service
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "placeholderfixture_Restricted.kt" }.readText()
        assertTrue(
            "@OptIn(ExperimentalMarker::class)" in source,
            "Expected PlaceholderRestricted to opt in on ExperimentalMarker's behalf:\n$source",
        )
    }
}
