package tests

import org.kodein.mock.Mocker
import org.kodein.mock.mockFunction1
import kotlin.test.Test
import kotlin.test.assertEquals

// The generated placeholder provider is registered by a MockXxx constructor, so a Mocker that only
// ever mocks functional types never gets one. Deliberately not a TestsWithMocks: injectMocks would
// construct mocks and register the provider, hiding what these tests cover.
class BareMockerTests {

    @Test
    fun testFunctionMockWithBuiltinArgument() {
        val mocker = Mocker()
        val cb: (String) -> Unit = mockFunction1(mocker)

        mocker.every { cb(isAny()) } returns Unit

        cb("test")

        mocker.verify { cb(isAny()) }
    }

    @Test
    fun testFunctionMockWithCollectionArgument() {
        val mocker = Mocker()
        val cb: (List<String>) -> Unit = mockFunction1(mocker)

        mocker.every { cb(isAny()) } returns Unit

        cb(listOf("test"))

        mocker.verify { cb(isAny()) }
    }

    // Hand-written rather than mocker.mock<T>(): creating a generated mock would install the
    // placeholder provider, which is exactly what this must do without.
    class ManualMock(private val mocker: Mocker) {
        var name: String
            get() = mocker.register(this, "get:name")
            set(value) {
                mocker.register<Unit>(this, "set:name", value)
            }
    }

    @Test
    fun testBackProperty() {
        val mocker = Mocker()
        val mock = ManualMock(mocker)

        mocker.backProperty(mock, ManualMock::name, "default")

        assertEquals("default", mock.name)
        mock.name = "changed"
        assertEquals("changed", mock.name)
    }
}
