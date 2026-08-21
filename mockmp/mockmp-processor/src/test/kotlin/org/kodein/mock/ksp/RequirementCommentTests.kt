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
 * Asserts the `/* Required by: ... */` comment [MocKMPProcessor.requirementComment] attaches above
 * every generated `MockXxx` class and `fakeXxx()` function — the annotation that started the chain
 * ([ToProcess.origin]), then each hop of [ToProcess.path] that led from it to this exact declaration.
 * [FakeGenerationTests] covers generated member shape; this covers the comment that explains why the
 * declaration exists at all, so it reads the source KSP actually wrote.
 *
 * KSP2, matching the build (`symbol-processing-aa-embeddable`); [useKsp2] is what selects it.
 */
@OptIn(ExperimentalCompilerApi::class)
class RequirementCommentTests {

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
    fun transitiveFakeCommentNamesTheAnnotationAndEachHop() {
        // The same Database/Connection/Network/TCPLayer shape ProcessorErrorTests.unfakeableTypeReportsWhatRequiredIt
        // pins the "Required by:" wording of, minus TCPLayer's private constructor so the chain
        // actually succeeds and reaches generation instead of aborting the round.
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.UsesFakes

            class TCPLayer

            class Network(val tcp: TCPLayer, val timeout: Int)

            interface Connection {
                fun connect(creds: String): Network
            }

            interface Database {
                val con: Connection
            }

            @UsesFakes(Database::class)
            class Tests
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()

        // Requested directly: the origin line, and nothing else — no "->" hop to explain.
        val databaseSource = generated.first { it.name == "fakefixture_Database.kt" }.readText()
        assertTrue("Required by:" in databaseSource)
        assertTrue("@UsesFakes on fixture.Tests" in databaseSource)
        assertFalse("->" in databaseSource, "Did not expect a hop for a directly requested fake:\n$databaseSource")

        // Reached transitively, two hops deep: Database.con -> Connection.connect(..).
        val networkSource = generated.first { it.name == "fakefixture_Network.kt" }.readText()
        val originIndex = networkSource.indexOf("@UsesFakes on fixture.Tests")
        val firstHopIndex = networkSource.indexOf("-> Database.con: Connection")
        val secondHopIndex = networkSource.indexOf("-> Connection.connect(String): Network")
        val declIndex = networkSource.indexOf("internal fun fakefixture_Network")
        assertTrue(originIndex >= 0, "Expected the @UsesFakes origin in:\n$networkSource")
        assertTrue(firstHopIndex > originIndex, "Expected the Database.con hop after the origin:\n$networkSource")
        assertTrue(secondHopIndex > firstHopIndex, "Expected the Connection.connect hop after Database.con:\n$networkSource")
        assertTrue(declIndex > secondHopIndex, "Expected the comment above the fakeXxx() function:\n$networkSource")

        // Not KDoc: the processor downgrades the two-star opener to a plain block comment.
        assertFalse("/**" in networkSource, "Did not expect a KDoc marker:\n$networkSource")
        assertTrue(networkSource.trimStart().startsWith("package fixture"))
    }

    @Test
    fun mockCommentsNameTheirAnnotationSite() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock
            import org.kodein.mock.UsesMocks

            interface ServiceA {
                val name: String
            }

            interface ServiceB {
                val name: String
            }

            @UsesMocks(ServiceA::class)
            class Tests {
                @Mock
                lateinit var serviceB: ServiceB
            }
            """,
            options = mapOf("org.kodein.mock.multiplatform" to "false"),
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().toList()

        val mockA = generated.first { it.name == "MockServiceA.kt" }.readText()
        val originIndexA = mockA.indexOf("@UsesMocks on fixture.Tests")
        val classIndexA = mockA.indexOf("class MockServiceA")
        assertTrue(originIndexA >= 0, "Expected the @UsesMocks origin in:\n$mockA")
        assertTrue(classIndexA > originIndexA, "Expected the comment above the class declaration:\n$mockA")

        val mockB = generated.first { it.name == "MockServiceB.kt" }.readText()
        assertTrue("@Mock fixture.Tests.serviceB" in mockB)
    }

    /**
     * The two ways a type can be discovered without ever being named by the user: as a return type of
     * an *interface*'s abstract member (routed to [MocKMPProcessor.addMock], since it can be
     * implemented), and as a return type resolving to a plain *class* (routed to
     * [MocKMPProcessor.addFake], since it can be constructed). Both inherit the mocked interface's own
     * origin — there is nothing else to attribute them to — plus one path hop for the member that
     * needed them.
     */
    @Test
    fun implicitlySeededMockAndFakeInheritTheMockedInterfacesOriginPlusAHop() {
        val compilation = compilation(
            """
            package fixture

            import org.kodein.mock.Mock

            class Payload(val value: String)

            interface Identified {
                override fun equals(other: Any?): Boolean
                override fun hashCode(): Int
                fun doSomething()
            }

            interface Service {
                fun payload(): Payload
                fun identify(): Identified
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

        val fakePayload = generated.first { it.name == "fakefixture_Payload.kt" }.readText()
        assertTrue("@Mock fixture.Tests.service" in fakePayload)
        assertTrue("-> Service.payload(): Payload" in fakePayload)

        val mockIdentified = generated.first { it.name == "MockIdentified.kt" }.readText()
        assertTrue("@Mock fixture.Tests.service" in mockIdentified)
        assertTrue("-> Service.identify(): Identified" in mockIdentified)
    }
}
