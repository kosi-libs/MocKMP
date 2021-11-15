package tests

import foo.MockBar
import org.kodein.micromock.Mocker
import kotlin.test.*

class captureTests {

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
    }

    @Test
    fun lambdaCaptureAtDefinition() {
        val bar = MockBar(mocker)
        val captures = ArrayList<(String) -> Int>()
        mocker.on { bar.callback(isAny(captures)) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun lambdaCaptureAtVerification() {
        val bar = MockBar(mocker)
        mocker.on { bar.callback(isAny()) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        val captures = ArrayList<(String) -> Int>()
        mocker.verify { bar.callback(isAny(captures)) }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

}
