package tests

import org.kodein.mock.Mocker
import org.kodein.mock.mockFunction1
import kotlin.test.Test

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
}
