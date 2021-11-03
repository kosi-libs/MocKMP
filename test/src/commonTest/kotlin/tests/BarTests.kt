package tests

import foo.Bar
import org.kodein.micromock.Mock
import org.kodein.micromock.Mocker
import kotlin.test.BeforeTest
import kotlin.test.Test

class BarTests {

    @set:Mock
    lateinit var bar: Bar

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
        injectMocks(mocker)
    }

    @Test
    fun testFoo() {
        mocker.every { bar.doString(isAny()) } returns Unit

        bar.doString("Salomon")

        mocker.verify { bar.doString("Salomon") }
    }
}
