package tests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.mockFunction10
import org.kodein.mock.mockFunction3
import org.kodein.mock.mockFunction4
import org.kodein.mock.mockFunction5
import org.kodein.mock.mockFunction6
import org.kodein.mock.mockFunction7
import org.kodein.mock.mockFunction8
import org.kodein.mock.mockFunction9
import org.kodein.mock.mockSuspendFunction10
import org.kodein.mock.mockSuspendFunction3
import org.kodein.mock.mockSuspendFunction4
import org.kodein.mock.mockSuspendFunction5
import org.kodein.mock.mockSuspendFunction6
import org.kodein.mock.mockSuspendFunction7
import org.kodein.mock.mockSuspendFunction8
import org.kodein.mock.mockSuspendFunction9
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * mockFunctionN and mockSuspendFunctionN are 42 hand-written declarations (arities 0 to 10, each with a
 * type-string overload and a reified one). Arities 0, 1 and 2 are covered elsewhere — [BehaviourTests],
 * [BareMockerTests] and [InjectionTests] — leaving 3 to 10 built by nothing.
 *
 * The mistakes a copy-paste family invites are positional: a transposed argument in
 * `register(rec, method, a1, a2, …)`, a wrong index in a block's `it[0] as A1, it[1] as A2, …`
 * unpacking, or a mis-ordered type in the `"$functionName($a1Type, $a2Type, …)"` key. So every position
 * here carries a *distinct type and a distinct value*: types alone would not do it, since Kotlin/JS
 * represents Short, Int and Double all as `number` and a swapped cast need not throw there.
 *
 * Each test drives both routes into its arity: the generated `@Mock` property, which the processor
 * fills with `mockFunctionN(this, a1Type = …, …)` and no block, and a hand-written
 * `mockFunctionN(mocker) { … }`, which is the reified overload plus the block path.
 */
class FunctionArityTests : TestsWithMocks() {

    override fun setUpMocks() = mocker.injectMocks(this)

    // The ten argument values, in the order every arity below uses them. All ten simple names differ,
    // so the registration key is position-sensitive too. All ten are types the processor treats as
    // builtins, deliberately: the block form below expands to `every { it(isAny(), …) }`, so every
    // argument type needs a placeholder, and a type without one (an enum, say) fails there rather
    // than testing anything about arity.
    private val a1 = 1
    private val a2 = "two"
    private val a3 = true
    private val a4 = 4L
    private val a5 = 5.0
    private val a6 = 6f
    private val a7: Short = 7
    private val a8: Byte = 8
    private val a9 = listOf("nine")
    private val a10 = setOf("ten")

    @Mock lateinit var f3: (Int, String, Boolean) -> String
    @Mock lateinit var f4: (Int, String, Boolean, Long) -> String
    @Mock lateinit var f5: (Int, String, Boolean, Long, Double) -> String
    @Mock lateinit var f6: (Int, String, Boolean, Long, Double, Float) -> String
    @Mock lateinit var f7: (Int, String, Boolean, Long, Double, Float, Short) -> String
    @Mock lateinit var f8: (Int, String, Boolean, Long, Double, Float, Short, Byte) -> String
    @Mock lateinit var f9: (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>) -> String
    @Mock lateinit var f10: (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>, Set<String>) -> String

    @Mock lateinit var sf3: suspend (Int, String, Boolean) -> String
    @Mock lateinit var sf4: suspend (Int, String, Boolean, Long) -> String
    @Mock lateinit var sf5: suspend (Int, String, Boolean, Long, Double) -> String
    @Mock lateinit var sf6: suspend (Int, String, Boolean, Long, Double, Float) -> String
    @Mock lateinit var sf7: suspend (Int, String, Boolean, Long, Double, Float, Short) -> String
    @Mock lateinit var sf8: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte) -> String
    @Mock lateinit var sf9: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>) -> String
    @Mock lateinit var sf10: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>, Set<String>) -> String

    @Test
    fun testArity3() {
        every { f3(a1, a2, a3) } returns "generated"
        assertEquals("generated", f3(a1, a2, a3))
        verify { f3(a1, a2, a3) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean) -> String =
            mockFunction3(mocker) { p1, p2, p3 -> received = listOf(p1, p2, p3); "hand" }
        assertEquals("hand", hand(a1, a2, a3))
        assertEquals(listOf<Any?>(a1, a2, a3), received)
    }

    @Test
    fun testArity4() {
        every { f4(a1, a2, a3, a4) } returns "generated"
        assertEquals("generated", f4(a1, a2, a3, a4))
        verify { f4(a1, a2, a3, a4) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long) -> String =
            mockFunction4(mocker) { p1, p2, p3, p4 -> received = listOf(p1, p2, p3, p4); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4))
        assertEquals(listOf<Any?>(a1, a2, a3, a4), received)
    }

    @Test
    fun testArity5() {
        every { f5(a1, a2, a3, a4, a5) } returns "generated"
        assertEquals("generated", f5(a1, a2, a3, a4, a5))
        verify { f5(a1, a2, a3, a4, a5) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double) -> String =
            mockFunction5(mocker) { p1, p2, p3, p4, p5 -> received = listOf(p1, p2, p3, p4, p5); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5), received)
    }

    @Test
    fun testArity6() {
        every { f6(a1, a2, a3, a4, a5, a6) } returns "generated"
        assertEquals("generated", f6(a1, a2, a3, a4, a5, a6))
        verify { f6(a1, a2, a3, a4, a5, a6) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double, Float) -> String =
            mockFunction6(mocker) { p1, p2, p3, p4, p5, p6 -> received = listOf(p1, p2, p3, p4, p5, p6); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6), received)
    }

    @Test
    fun testArity7() {
        every { f7(a1, a2, a3, a4, a5, a6, a7) } returns "generated"
        assertEquals("generated", f7(a1, a2, a3, a4, a5, a6, a7))
        verify { f7(a1, a2, a3, a4, a5, a6, a7) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double, Float, Short) -> String =
            mockFunction7(mocker) { p1, p2, p3, p4, p5, p6, p7 -> received = listOf(p1, p2, p3, p4, p5, p6, p7); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7), received)
    }

    @Test
    fun testArity8() {
        every { f8(a1, a2, a3, a4, a5, a6, a7, a8) } returns "generated"
        assertEquals("generated", f8(a1, a2, a3, a4, a5, a6, a7, a8))
        verify { f8(a1, a2, a3, a4, a5, a6, a7, a8) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double, Float, Short, Byte) -> String =
            mockFunction8(mocker) { p1, p2, p3, p4, p5, p6, p7, p8 -> received = listOf(p1, p2, p3, p4, p5, p6, p7, p8); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8), received)
    }

    @Test
    fun testArity9() {
        every { f9(a1, a2, a3, a4, a5, a6, a7, a8, a9) } returns "generated"
        assertEquals("generated", f9(a1, a2, a3, a4, a5, a6, a7, a8, a9))
        verify { f9(a1, a2, a3, a4, a5, a6, a7, a8, a9) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>) -> String =
            mockFunction9(mocker) { p1, p2, p3, p4, p5, p6, p7, p8, p9 -> received = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8, a9))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8, a9), received)
    }

    @Test
    fun testArity10() {
        every { f10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) } returns "generated"
        assertEquals("generated", f10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10))
        verify { f10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) }

        var received: List<Any?> = emptyList()
        val hand: (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>, Set<String>) -> String =
            mockFunction10(mocker) { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ->
                received = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10); "hand"
            }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10), received)
    }

    // Pins the widest registration key. Only the simple names are asserted, and only that they appear
    // in order: bestName() renders qualifiedName on JVM/Native but simpleName on JS/Wasm, so the whole
    // key differs by platform (same reason as InjectionTests.testCallbackOfOneArgumentRegistrationKey).
    @Test
    fun testArity10RegistrationKey() {
        val ex = assertFailsWith<Mocker.MockingException> { f10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) }
        val message = ex.message!!
        assertTrue("invoke(" in message, message)
        val names = listOf("Int", "String", "Boolean", "Long", "Double", "Float", "Short", "Byte", "List", "Set")
        var from = message.indexOf("invoke(")
        names.forEach { name ->
            val at = message.indexOf(name, from)
            assertTrue(at >= 0, "expected '$name' after index $from in: $message")
            from = at + name.length
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity3() = runTest {
        everySuspending { sf3(a1, a2, a3) } returns "generated"
        assertEquals("generated", sf3(a1, a2, a3))
        verifyWithSuspend { sf3(a1, a2, a3) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean) -> String =
            mockSuspendFunction3(mocker) { p1, p2, p3 -> received = listOf(p1, p2, p3); "hand" }
        assertEquals("hand", hand(a1, a2, a3))
        assertEquals(listOf<Any?>(a1, a2, a3), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity4() = runTest {
        everySuspending { sf4(a1, a2, a3, a4) } returns "generated"
        assertEquals("generated", sf4(a1, a2, a3, a4))
        verifyWithSuspend { sf4(a1, a2, a3, a4) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long) -> String =
            mockSuspendFunction4(mocker) { p1, p2, p3, p4 -> received = listOf(p1, p2, p3, p4); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4))
        assertEquals(listOf<Any?>(a1, a2, a3, a4), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity5() = runTest {
        everySuspending { sf5(a1, a2, a3, a4, a5) } returns "generated"
        assertEquals("generated", sf5(a1, a2, a3, a4, a5))
        verifyWithSuspend { sf5(a1, a2, a3, a4, a5) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double) -> String =
            mockSuspendFunction5(mocker) { p1, p2, p3, p4, p5 -> received = listOf(p1, p2, p3, p4, p5); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity6() = runTest {
        everySuspending { sf6(a1, a2, a3, a4, a5, a6) } returns "generated"
        assertEquals("generated", sf6(a1, a2, a3, a4, a5, a6))
        verifyWithSuspend { sf6(a1, a2, a3, a4, a5, a6) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double, Float) -> String =
            mockSuspendFunction6(mocker) { p1, p2, p3, p4, p5, p6 -> received = listOf(p1, p2, p3, p4, p5, p6); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity7() = runTest {
        everySuspending { sf7(a1, a2, a3, a4, a5, a6, a7) } returns "generated"
        assertEquals("generated", sf7(a1, a2, a3, a4, a5, a6, a7))
        verifyWithSuspend { sf7(a1, a2, a3, a4, a5, a6, a7) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double, Float, Short) -> String =
            mockSuspendFunction7(mocker) { p1, p2, p3, p4, p5, p6, p7 -> received = listOf(p1, p2, p3, p4, p5, p6, p7); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity8() = runTest {
        everySuspending { sf8(a1, a2, a3, a4, a5, a6, a7, a8) } returns "generated"
        assertEquals("generated", sf8(a1, a2, a3, a4, a5, a6, a7, a8))
        verifyWithSuspend { sf8(a1, a2, a3, a4, a5, a6, a7, a8) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte) -> String =
            mockSuspendFunction8(mocker) { p1, p2, p3, p4, p5, p6, p7, p8 -> received = listOf(p1, p2, p3, p4, p5, p6, p7, p8); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity9() = runTest {
        everySuspending { sf9(a1, a2, a3, a4, a5, a6, a7, a8, a9) } returns "generated"
        assertEquals("generated", sf9(a1, a2, a3, a4, a5, a6, a7, a8, a9))
        verifyWithSuspend { sf9(a1, a2, a3, a4, a5, a6, a7, a8, a9) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>) -> String =
            mockSuspendFunction9(mocker) { p1, p2, p3, p4, p5, p6, p7, p8, p9 -> received = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9); "hand" }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8, a9))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8, a9), received)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendArity10() = runTest {
        everySuspending { sf10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) } returns "generated"
        assertEquals("generated", sf10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10))
        verifyWithSuspend { sf10(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) }

        var received: List<Any?> = emptyList()
        val hand: suspend (Int, String, Boolean, Long, Double, Float, Short, Byte, List<String>, Set<String>) -> String =
            mockSuspendFunction10(mocker) { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 ->
                received = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10); "hand"
            }
        assertEquals("hand", hand(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10))
        assertEquals(listOf<Any?>(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10), received)
    }
}
