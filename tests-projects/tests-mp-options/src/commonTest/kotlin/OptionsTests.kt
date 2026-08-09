// The imports are the point: with accessorsPackage("custom.accessors"), the generated accessors live
// there and nowhere else. Importing them from the default org.kodein.mock.generated would not resolve.
import custom.accessors.fake
import custom.accessors.injectMocks
import custom.accessors.mock
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals

// This is what actually pins public(). Compiling only catches half of it on its own: Kotlin lets an
// `internal expect` be satisfied by a `public actual` — widening is legal — so the extract task
// dropping the option would slip through, even though the processor dropping it would not. A
// public inline function may not reach a non-public declaration, so this stops compiling the moment
// the accessors go back to being internal.
public inline fun <reified T : Any> Mocker.publicMock(): T = mock<T>()

interface Service {
    fun greet(name: String): String
}

data class Greeting(val text: String, val count: Int)

class OptionsTests : TestsWithMocks() {

    override fun setUpMocks() = mocker.injectMocks(this)

    @Mock lateinit var service: Service

    @Fake lateinit var greeting: Greeting

    @Test
    fun theInjectedMockWorks() {
        every { service.greet(isAny()) } returns "hello"
        assertEquals("hello", service.greet("world"))
        verify { service.greet("world") }
    }

    // The accessors reached directly rather than through injection, so `mock` and `fake` themselves
    // are resolved from the configured package.
    @Test
    fun theAccessorsComeFromTheConfiguredPackage() {
        val other = mocker.mock<Service>()
        every { other.greet(isAny()) } returns "bonjour"
        assertEquals("bonjour", other.greet("monde"))
        verify { other.greet("monde") }

        assertEquals(Greeting("", 0), fake<Greeting>())
    }

    @Test
    fun theFakeIsInjected() {
        assertEquals(Greeting("", 0), greeting)
    }
}
