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
 * Asserts that every file this processor writes carries `@file:Suppress("DEPRECATION",
 * "DEPRECATION_ERROR")` — needed the moment a mocked/faked declaration's own signature (supertype,
 * member parameter/return type, constructor argument, enum entry, `T::class`/`typeOf<T>()` dispatch
 * key) mentions something `@Deprecated`, which the *generated* file has no way to avoid mentioning
 * and no way for the user to edit around. `allWarningsAsErrors = true` on every [KotlinCompilation]
 * here is the real assertion, exactly as [RequiresOptInTests] uses it for opt-in: without
 * [MocKMPProcessor.suppressDeprecation], every one of these fails to compile once a consumer turns
 * warnings-as-errors on, as `tests-projects` now does.
 *
 * Each fixture suppresses `DEPRECATION` at its own use sites (`@file:Suppress` on the fixture source,
 * or a member-level `@Suppress` where only one member needs it) — the same thing a real user has to do
 * at the one site they can actually edit; this file is only responsible for what the *generated* code
 * does with it.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class DeprecationTests {

    private fun compilation(
        source: String,
        options: Map<String, String> = emptyMap(),
    ): KotlinCompilation =
        KotlinCompilation().apply {
            sources = listOf(SourceFile.kotlin("Fixture.kt", source))
            inheritClassPath = true
            allWarningsAsErrors = true
            useKsp2()
            configureKsp {
                symbolProcessorProviders += MocKMPProcessorProvider()
                processorOptions.putAll(options)
            }
        }

    /**
     * Asserts [fileName] carries the file-level suppress every generated file must start with, ahead
     * of its `package` line. KotlinPoet wraps a two-member `@file:Suppress(...)` onto its own lines
     * (`@file:Suppress(\n  "DEPRECATION",\n  "DEPRECATION_ERROR",\n)`) rather than one, so this checks
     * the header — everything before `package` — for the annotation and both members, instead of
     * matching one exact line.
     */
    private fun List<java.io.File>.assertFileSuppressesDeprecation(fileName: String) {
        val source = first { it.name == fileName }.readText()
        val header = source.substringBefore("\npackage ")
        assertTrue("@file:Suppress(" in header, "Expected $fileName to open with @file:Suppress(...):\n$source")
        assertTrue("\"DEPRECATION\"" in header, "Expected $fileName's file annotation to suppress DEPRECATION:\n$source")
        assertTrue("\"DEPRECATION_ERROR\"" in header, "Expected $fileName's file annotation to suppress DEPRECATION_ERROR:\n$source")
    }

    /**
     * `CardGame`/`Suit` shape: a mocked interface whose only abstract member takes a nullable
     * parameter of a deprecated enum type. [Round.seedPlaceholders] seeds a placeholder for that enum
     * under its non-nullable form regardless of nullability, so this alone is enough to reach
     * [Round.generatePlaceholderFunction] and [Round.generatePlaceholderAccessor] for it, on top of
     * [Round.generateMockClass] and [Round.generateMockAccessor] for the interface itself.
     */
    @Test
    fun mockedDeprecatedInterfaceWithDeprecatedEnumParameterCompiles() {
        val compilation = compilation(
            """
            @file:Suppress("DEPRECATION")

            package fixture

            import org.kodein.mock.Mock

            @Deprecated("for test")
            enum class Suit { CLUBS, DIAMONDS }

            @Deprecated("for test")
            interface CardGame {
                fun play(@Suppress("DEPRECATION") suit: Suit?)
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
        listOf("MockCardGame.kt", "placeholderfixture_Suit.kt", "mocks.kt", "placeholders.kt")
            .forEach { generated.assertFileSuppressesDeprecation(it) }
    }

    /**
     * A faked (not mocked) deprecated class: covers `fakes.kt`'s generated
     * `typeOf<DeprecatedData>()` property — a declaration distinct from the `fake(KType)` function
     * that merely refers to it by name, so it needs its own file-level coverage too, not just the
     * function's.
     */
    @Test
    fun fakedDeprecatedClassCompiles() {
        val compilation = compilation(
            """
            @file:Suppress("DEPRECATION")

            package fixture

            import org.kodein.mock.UsesFakes

            @Deprecated("for test")
            class DeprecatedData(val value: Int = 0)

            @UsesFakes(DeprecatedData::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()
        listOf("fakefixture_DeprecatedData.kt", "fakes.kt")
            .forEach { generated.assertFileSuppressesDeprecation(it) }
    }

    /**
     * `DeprecationLevel.ERROR` — a type deprecated at the level the Kotlin compiler itself refuses to
     * let a caller opt out of at the reference site (`@Suppress("DEPRECATION_ERROR")` on the *user's*
     * `@Mock` property would not compile even without `allWarningsAsErrors`). Only the generated
     * file's own `DEPRECATION_ERROR` suppression makes this mockable at all.
     */
    @Test
    fun errorLevelDeprecatedInterfaceCompiles() {
        val compilation = compilation(
            """
            @file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

            package fixture

            import org.kodein.mock.Mock

            @Deprecated("for test", level = DeprecationLevel.ERROR)
            interface Legacy {
                fun run()
            }

            class Tests {
                @Mock
                lateinit var legacy: Legacy
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        compilation.workingDir.walkTopDown().toList().assertFileSuppressesDeprecation("MockLegacy.kt")
    }
}
