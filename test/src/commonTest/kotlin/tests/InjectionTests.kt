package tests

import data.Data
import data.Direction
import data.SomeDirection
import data.SubData
import foo.Bar
import foo.MockBar
import org.kodein.micromock.Fake
import org.kodein.micromock.Mock
import org.kodein.micromock.Mocker
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InjectionTests {

    @set:Mock
    lateinit var bar: Bar

    @set:Fake
    lateinit var data: Data

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
        injectMocks(mocker)
    }

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
}
