package tests

import data.*
import foo.Bar
import foo.Foo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.*
import kotlin.test.assertContains
import kotlin.time.Instant

class InjectionTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data

    @Fake
    lateinit var fooData: foo.Data

    @Fake
    lateinit var genData: GenData<Data>

    @Fake
    lateinit var genFooData: GenData<foo.Data>

    @Fake
    lateinit var arrays: Arrays

    @Fake
    lateinit var funs: Funs

    // An interface is faked by implementing it; a concrete instantiation of a generic one is only
    // reachable through a declared property like this, as it is for a generic class fake.
    @Fake
    lateinit var service: Service

    @Fake
    lateinit var stringBox: Box<String>

    @Mock
    lateinit var callback: (Boolean, Int) -> String

    @Mock
    lateinit var callback1: (String) -> Unit

    @Mock
    lateinit var suspendCallback: suspend (String) -> Int

    @Mock
    lateinit var s1: Foo.Sub

    @Mock
    lateinit var s2: Bar.Sub

    val control by withMocks { Control(bar, data) }

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testMockInjection() {
        assertNotNull(bar)
        assertNotNull(data)
        assertNotNull(fooData)
        assertNotNull(genData)
        assertNotNull(genFooData)
        assertNotNull(arrays)
        assertNotNull(funs)
        assertNotNull(service)
        assertNotNull(stringBox)
        assertNotNull(callback)
        assertNotNull(s1)
        assertNotNull(s2)
    }

    @Test
    fun testFakeData() {
        assertEquals(
            Data(
                GenData("", 0),
                GenData(0, 0),
                GenData(emptyMap(), 0),
                Data.SubData(null),
                null,
                SomeDirection(Direction.LEFT, SomeDirection.SubData(null)),
                SomeDirection(Direction.LEFT, SomeDirection.SubData(null)),
                Instant.fromEpochSeconds(0),
                // java.lang.Exception has no structural equals(), so reuse the faked instance's own
                // exception reference here; `code` is still checked against the literal below.
                Error(0, data.special2.exception),
                emptyList(),
                ArrayList(),
                ArrayDeque(),
                emptySet(),
                HashSet(),
                LinkedHashSet(),
                emptyMap(),
                HashMap(),
                LinkedHashMap(),
            ),
            data
        )
    }

    @Test
    fun testFakeInterface() {
        assertEquals("", service.name)
        service.record("entry")
        assertEquals("", stringBox.content)
        assertEquals("", stringBox.replace("other"))
    }

    @Test
    fun testFakeArray() {
        assertEquals(0, arrays.bytes.size)
        assertEquals(0, arrays.strings.size)
    }

    @Test
    fun testDeferred() {
        every { bar.doData(isAny()) } returns Unit
        control.doIt()
        verify { bar.doData(data) }
    }

    @Test
    fun testCallback() {
        every { callback(isAny(), isAny()) } returns "test"
        callback(true, 42)
        verify { callback(true, 42) }
    }

    @Test
    fun testCallbackOfOneArgument() {
        every { callback1(isAny()) } returns Unit
        callback1("test")
        verify { callback1("test") }
    }

    @Test
    fun testCallbackOfOneArgumentRegistrationKey() {
        val ex = assertFailsWith<Mocker.MockingException> { callback1("test") }
        // The whole key, on every platform. A generated mock is handed the qualified name the
        // processor resolved -- mockFunction1(this, a1Type = "kotlin.String") -- rather than deriving
        // it from bestName(), which is exactly why the type-string overloads exist. This asserts that
        // guarantee; PlatformKeyTests asserts the reified overloads, which do vary by platform.
        assertContains(ex.message!!, "invoke(kotlin.String)")
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendCallback() = runTest {
        everySuspending { suspendCallback(isAny()) } returns 42

        assertEquals(42, suspendCallback("test"))

        verifyWithSuspend { suspendCallback("test") }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendCallbackRegistrationKey() = runTest {
        val ex = assertFailsWith<Mocker.MockingException> { suspendCallback("test") }
        // "invoke(" would not discriminate: the old MockSuspendFunction1 route registered
        // "invoke(?)", erasing the argument type out of the key.
        assertTrue("String" in ex.message!!, ex.message)
    }

    @Test
    fun testFakeFunctions() {
        funs.cb("foo")
        assertEquals(GenData("", 0), funs.data())
        assertEquals(GenData("", 0), funs.combo("foo"))
    }

    @Test
    fun testSameName() {
        var r1 = false
        var r2 = false
        every { s1.doOp() } runs { r1 = true }
        every { s2.doOp() } runs { r2 = true }
        assertFalse(r1 || r2)
        s1.doOp()
        verify { s1.doOp() }
        assertTrue(r1)
        assertFalse(r2)
        s2.doOp()
        verify { s2.doOp() }
        assertTrue(r2)
    }
}
