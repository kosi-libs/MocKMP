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
    // reason, then the @FakeProvider suggestion. An interface cannot be instantiated, so @UsesFakes on
    // one is the shortest way in.
    @Test
    fun unfakeableTypeStatesAReasonAndSuggestsFakeProvider() {
        val result = compile(
            """
            import org.kodein.mock.UsesFakes

            interface NotConstructible

            @UsesFakes(NotConstructible::class)
            class Tests
            """
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode, result.messages)
        assertContains(result.messages, "Cannot generate a fake for NotConstructible")
        assertContains(result.messages, "interfaces cannot be instantiated")
        assertContains(result.messages, "register a top-level @FakeProvider function")
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
