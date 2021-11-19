package tests

import data.*
import foo.Bar
import foo.MockBar
import org.kodein.micromock.Fake
import org.kodein.micromock.Mock
import org.kodein.micromock.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InjectionTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data

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
                SubData("", 0),
                null,
                SomeDirection(Direction.LEFT)
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
}
