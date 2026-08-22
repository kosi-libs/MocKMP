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
 * Covers the third generated kind, the Placeholder: a type reachable only from a mocked interface's
 * abstract property types and abstract function *parameter* types (never its return types — nothing
 * but `providePlaceholder`'s argument-constraint fallback is ever meant to need one), and from such a
 * type's own constructor dependencies. [FakeGenerationTests]/[RequirementCommentTests] cover the
 * mocked-interface-member seeding itself; this covers the placeholder generated from it: that it is
 * neither a Mock nor a Fake, that its abstract members throw, that its own constructor dependencies
 * resolve to an existing Fake or Mock in preference to a new Placeholder, and that `providePlaceholder`
 * ranks Fake above Mock above Placeholder.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class PlaceholderGenerationTests {

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

    @Test
    fun mockedInterfacesAbstractPropertyTypeYieldsAPlaceholderAndNothingElse() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            interface Config

            interface Service {
                val config: Config
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
        assertTrue(generated.any { it.name == "placeholderfixture_Config.kt" }, "Expected a placeholderConfig() function")
        assertFalse(generated.any { it.name == "MockConfig.kt" }, "Did not expect a Mock for Config")
        assertFalse(generated.any { it.name == "fakefixture_Config.kt" }, "Did not expect a Fake for Config")

        val placeholders = generated.first { it.name == "placeholders.kt" }.readText()
        assertTrue("Config::class ->" in placeholders, "Expected a providePlaceholder branch for Config:\n$placeholders")

        val mocks = generated.first { it.name == "mocks.kt" }.readText()
        val fakes = generated.first { it.name == "fakes.kt" }.readText()
        assertFalse("Config" in mocks, "Did not expect Config in the mock(KClass) accessor:\n$mocks")
        assertFalse("Config" in fakes, "Did not expect Config in the fake(KType) accessor:\n$fakes")
    }

    @Test
    fun placeholderMembersAllThrow() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            interface Options {
                val flag: Boolean
                var name: String
            }

            interface Service {
                val options: Options
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

        val source = compilation.workingDir.walkTopDown().first { it.name == "placeholderfixture_Options.kt" }.readText()
        assertTrue("class PlaceholderOptions" in source, "Expected a private PlaceholderOptions implementation:\n$source")
        val throwCount = Regex("""error\("Placeholders are not meant to be used"\)""").findAll(source).count()
        // flag's getter, name's getter, and name's setter: three throwing members.
        assertEquals(3, throwCount, "Expected every abstract member to throw:\n$source")
    }

    @Test
    fun placeholdersConstructorDependencyPrefersAnExistingFakeOrMockOverANewPlaceholder() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock
            import org.kodein.mock.UsesFakes
            import org.kodein.mock.UsesMocks

            class FakedDep(val x: Int)
            interface MockedDep

            class ViaFake(val dep: FakedDep)
            class ViaMock(val dep: MockedDep)
            class ViaNothing(val name: String)

            interface Service {
                fun handle(a: ViaFake, b: ViaMock, c: ViaNothing)
            }

            @UsesFakes(FakedDep::class)
            @UsesMocks(MockedDep::class)
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

        // FakedDep and MockedDep are covered directly — no redundant Placeholder for either.
        assertFalse(generated.any { it.name == "placeholderfixture_FakedDep.kt" }, "Did not expect a Placeholder for FakedDep")
        assertFalse(generated.any { it.name == "placeholderfixture_MockedDep.kt" }, "Did not expect a Placeholder for MockedDep")

        val viaFake = generated.first { it.name == "placeholderfixture_ViaFake.kt" }.readText()
        assertTrue("fakefixture_FakedDep()" in viaFake, "Expected ViaFake's constructor to reuse the existing Fake:\n$viaFake")

        val viaMock = generated.first { it.name == "placeholderfixture_ViaMock.kt" }.readText()
        assertTrue("MockMockedDep(" in viaMock, "Expected ViaMock's constructor to reuse the existing Mock:\n$viaMock")

        val viaNothing = generated.first { it.name == "placeholderfixture_ViaNothing.kt" }.readText()
        assertTrue("\"\"" in viaNothing, "Expected ViaNothing's constructor to use the builtin empty string:\n$viaNothing")
    }

    @Test
    fun aTypeWithBothAFakeAndAMockResolvesToTheFakeInProvidePlaceholder() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes
            import org.kodein.mock.UsesMocks

            interface Both

            @UsesFakes(Both::class)
            @UsesMocks(Both::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val placeholders = compilation.workingDir.walkTopDown().first { it.name == "placeholders.kt" }.readText()
        val branches = Regex("""Both::class ->""").findAll(placeholders).count()
        assertEquals(1, branches, "Expected exactly one Both::class branch (only the first ever runs):\n$placeholders")
        val branchLine = placeholders.lineSequence().first { "Both::class ->" in it }
        assertTrue("fakefixture_Both()" in branchLine, "Expected the Fake to win over the Mock:\n$branchLine")
    }

    @Test
    fun placeholderConstructorDependencyIsGeneratedAtItsExactInstantiation() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            @JvmInline
            value class Id<T>(val id: String)
            class Feature(val id: Id<String>)

            interface Service {
                fun start(feature: Feature)
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
        val idPlaceholder = generated.first { it.name == "placeholderfixture_IdXkotlin_StringX.kt" }.readText()
        assertTrue(": Id<String> = Id(id = \"\")" in idPlaceholder, "Expected a concrete Id<String> placeholder:\n$idPlaceholder")

        val featurePlaceholder = generated.first { it.name == "placeholderfixture_Feature.kt" }.readText()
        assertTrue("placeholderfixture_IdXkotlin_StringX()" in featurePlaceholder, "Expected Feature's constructor to call the Id<String> placeholder:\n$featurePlaceholder")

        // Id's declaration is reachable only through this one instantiation, so providePlaceholder
        // must still collapse it to exactly one branch (only the first matching one of a `when` ever
        // runs — same invariant aTypeWithBothAFakeAndAMockResolvesToTheFakeInProvidePlaceholder checks
        // for a Fake/Mock clash).
        val placeholders = generated.first { it.name == "placeholders.kt" }.readText()
        val branches = Regex("""Id::class ->""").findAll(placeholders).count()
        assertEquals(1, branches, "Expected exactly one Id::class branch:\n$placeholders")
    }

    @Test
    fun placeholderConstructorDependencyOnAMockedGenericInterfaceInstantiatesTheMockAtTheRequiredType() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock
            import org.kodein.mock.UsesMocks

            interface Gen<out T : Any>
            class Holder(val gen: Gen<String>)

            interface Service {
                fun use(holder: Holder)
            }

            @UsesMocks(Gen::class)
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
        val holderPlaceholder = generated.first { it.name == "placeholderfixture_Holder.kt" }.readText()
        assertTrue(
            "MockGen<String>(" in holderPlaceholder,
            "Expected Holder's constructor to instantiate MockGen at String, not at Gen's own Any bound:\n$holderPlaceholder",
        )
    }
}
