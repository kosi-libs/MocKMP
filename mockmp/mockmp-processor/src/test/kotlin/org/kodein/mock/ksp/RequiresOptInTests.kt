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
 * Asserts that generated code opts in wherever it *mentions* a `kotlin.RequiresOptIn`-marked
 * declaration — a return type, a referenced object/enum entry, a `::class` literal — even when the
 * mention carries no annotation of its own but one of its lexical ancestors does. This is the bug
 * kotlinx.serialization's `SerialKind`/`PolymorphicKind.OPEN` triggers in the real world:
 * `SerialKind` itself is unmarked, but [MocKMPProcessor.resolveSealedTarget] picks
 * `PolymorphicKind.OPEN` as its representative permitted subclass, and `PolymorphicKind` is
 * `@ExperimentalSerializationApi`.
 *
 * Distinct from [SubclassOptInRequiredTests], which covers `kotlin.SubclassOptInRequired` (opt-in for
 * *implementing/extending* a type) — this covers `kotlin.RequiresOptIn` (opt-in for *mentioning* one,
 * anywhere, propagated through lexical scope). Plain Kotlin stdlib, so — like that file — no extra
 * test dependency is needed.
 *
 * `KotlinCompilation.ExitCode.OK` is the real assertion: without
 * [MocKMPProcessor.requiredOptInMarkers], every one of these fails to compile (today it merely warns,
 * but the same gap is a hard error for any marker declared, like the stdlib default, with
 * `RequiresOptIn.Level.ERROR`) exactly like the real-world `SerialKind` case did.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class RequiresOptInTests {

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

    // A marker declared at RequiresOptIn's own default level (ERROR): the real-world bug only ever
    // warned, because kotlinx.serialization happens to use Level.WARNING — this pins that the fix
    // isn't relying on that leniency.
    private val markerFixture =
        """
        @RequiresOptIn
        annotation class ExperimentalMarker
        """.trimIndent()

    @Test
    fun sealedResolvingToAMarkedObjectOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            $markerFixture

            sealed class Picks {
                @ExperimentalMarker
                object Only : Picks()
            }

            @UsesFakes(Picks::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_Picks.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in source, "Expected fakePicks() to opt in:\n$source")
        assertTrue("Only" in source, "Expected the fake to resolve to the marked object:\n$source")
    }

    @Test
    fun markedEnumClassOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            $markerFixture

            @ExperimentalMarker
            enum class MarkedEnum { FIRST, SECOND }

            @OptIn(ExperimentalMarker::class)
            @UsesFakes(MarkedEnum::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_MarkedEnum.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in source, "Expected fakeMarkedEnum() to opt in:\n$source")
    }

    @Test
    fun markedConstructedClassOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            $markerFixture

            @ExperimentalMarker
            class MarkedClass(val x: Int)

            @OptIn(ExperimentalMarker::class)
            @UsesFakes(MarkedClass::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val source = compilation.workingDir.walkTopDown().first { it.name == "fakefixture_MarkedClass.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in source, "Expected fakeMarkedClass() to opt in:\n$source")
    }

    @Test
    fun markedInterfaceItselfOptsInWhenImplemented() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock
            import org.kodein.mock.UsesFakes
            import org.kodein.mock.UsesMocks

            $markerFixture

            @ExperimentalMarker
            interface MarkedInterface {
                fun doThing()
            }

            @OptIn(ExperimentalMarker::class)
            @UsesMocks(MarkedInterface::class)
            @UsesFakes(MarkedInterface::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        val mock = generated.first { it.name == "MockMarkedInterface.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in mock, "Expected MockMarkedInterface to opt in:\n$mock")
        val fake = generated.first { it.name == "fakefixture_MarkedInterface.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in fake, "Expected fakeMarkedInterface() to opt in:\n$fake")
    }

    /**
     * `Picks` reached only transitively, as a mocked interface's own property type — the closest
     * analogue to the real bug, which surfaced via `KSerializer.descriptor: SerialDescriptor.kind:
     * SerialKind`, itself only reached through a mocked interface's property. Exercises both
     * [MocKMPProcessor.generatePlaceholderFunction] and [MocKMPProcessor.generatePlaceholderAccessor]'s
     * `sealedObjectSubclasses` branch — the exact branch behind `PolymorphicKind.OPEN::class -> ...`.
     */
    @Test
    fun placeholderForSealedResolvingToAMarkedObjectOptsIn() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            $markerFixture

            sealed class Picks {
                @ExperimentalMarker
                object Only : Picks()
            }

            interface Service {
                val picks: Picks
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

        val generated = compilation.workingDir.walkTopDown().toList()
        val placeholder = generated.first { it.name == "placeholderfixture_Picks.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in placeholder, "Expected placeholderPicks() to opt in:\n$placeholder")

        val placeholders = generated.first { it.name == "placeholders.kt" }.readText()
        assertTrue("@OptIn(ExperimentalMarker::class)" in placeholders, "Expected providePlaceholder to opt in:\n$placeholders")
    }
}
