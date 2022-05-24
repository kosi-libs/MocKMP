package tests

import data.*
import foo.Bar
import foo.MockBar
import kotlinx.datetime.Instant
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InjectionTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data

    @Fake
    lateinit var funs: Funs

    @Mock
    lateinit var callback: (Boolean, Int) -> String

    val control by withMocks { Control(bar, data) }

    override fun setUpMocks() = injectMocks(mocker)

    @Test
    fun testMockInjection() {
        assertIs<MockBar>(bar)
    }

    @Test
    fun testFake() {
        assertEquals(
            Data(
                SubData("", 0),
                SubData(0, 0),
                SubData(emptyMap(), 0),
                null,
                SomeDirection(Direction.LEFT),
                SomeDirection(Direction.LEFT),
                Instant.fromEpochSeconds(0),
                emptyList(),
                emptyMap()
            ),
            data
        )
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
        assertEquals(SubData("", 0), funs.data())
        assertEquals(SubData("", 0), funs.combo("foo"))
    }
}
