package tests

import data.*
import foo.Bar
import foo.Foo
import foo.MockBar
import kotlinx.datetime.Instant
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.*

class InjectionTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data

    @Fake
    lateinit var arrays: Arrays

    @Fake
    lateinit var funs: Funs

    @Mock
    lateinit var callback: (Boolean, Int) -> String

    @Mock
    lateinit var s1: Foo.Sub

    @Mock
    lateinit var s2: Bar.Sub

    val control by withMocks { Control(bar, data) }

    override fun setUpMocks() = injectMocks(mocker)

    @Test
    fun testMockInjection() {
        assertIs<MockBar>(bar)
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
