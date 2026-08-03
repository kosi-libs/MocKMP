package tests

import data.Control
import data.Data
import foo.Bar
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class DeferredTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data

    val control by withMocks { Control(bar, data) }

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testWithMocksRecreatedOnEachSetUp() {
        val firstControl = control
        val firstBar = bar

        // What a framework that reuses the test instance does — JUnit 5's @TestInstance(PER_CLASS),
        // which cannot be used here since these sources are shared with the JUnit 4, JS, Wasm and
        // Native projects.
        injectMocksBeforeTest()

        assertNotSame(firstBar, bar)
        assertNotSame(firstControl, control)
        // The one that matters: holding the previous mock is what surfaces as "has not been mocked".
        assertSame(bar, control.bar)
    }
}
