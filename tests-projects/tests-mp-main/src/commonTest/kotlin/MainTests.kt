import org.kodein.mock.Mocker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTests {

    @Test
    fun testMain() {
        val mocker = Mocker()
        var barCalled = false
        val foo = mocker.mockFoo(
            onBar = { barCalled = true },
        )
        assertFalse(barCalled)
        foo.bar()
        assertTrue(barCalled)
    }

}