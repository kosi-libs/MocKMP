package tests

import data.SomeDirection
import foo.Bar
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.ITestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/** The superclass that forces the interface route in the first place. */
abstract class MySuperAbstractTests

/**
 * The helper's *other* entry point. Every other test here extends `TestsWithMocks`; this one implements
 * `ITestsWithMocks` directly, which is what a test class that already has a superclass has to do
 * (`helper.adoc`, "The ITestsWithMocks interface").
 *
 * That makes it the only check that `@BeforeTest` on the interface's `injectMocksBeforeTest` actually
 * fires for a direct implementor. `TestsWithMocks` contributes nothing but `mocksState`, so the
 * mechanism ought to be identical — but "ought to" is exactly what a test is for, and if it did not
 * fire, `bar` below would be uninitialised and every test in the class would fail loudly.
 */
class ITestsWithMocksTests : MySuperAbstractTests(), ITestsWithMocks {

    override val mocksState = ITestsWithMocks.State()

    override fun setUpMocks() = mocker.injectMocks(this)

    @Mock lateinit var bar: Bar

    @Fake lateinit var dir: SomeDirection

    val deferred by withMocks { Control(bar, dir) }

    class Control(val bar: Bar, val dir: SomeDirection)

    // initMocksBeforeTest runs last in injectMocksBeforeTest — after setUpMocks and initDeferred — so
    // everything is expected to be in place by the time it is called. Capturing here and comparing in
    // the test body is what pins that ordering.
    private var mockSeenByHook: Bar? = null
    private var deferredSeenByHook: Control? = null

    override fun initMocksBeforeTest() {
        mockSeenByHook = bar
        deferredSeenByHook = deferred
    }

    @Test
    fun theInterfaceInjectsMocksAndFakes() {
        assertNotNull(bar)
        assertNotNull(dir)
    }

    @Test
    fun everyAndVerifyWorkWithoutTheMockerReceiver() {
        every { bar.newString() } returns "through the interface"
        assertEquals("through the interface", bar.newString())
        verify { bar.newString() }
    }

    @Test
    fun initMocksBeforeTestRunsAfterInjection() {
        assertSame(bar, assertNotNull(mockSeenByHook), "the hook ran before the mocks were injected")
    }

    @Test
    fun initMocksBeforeTestRunsAfterDeferredInitialisation() {
        assertSame(deferred, assertNotNull(deferredSeenByHook), "the hook ran before withMocks was initialised")
        assertSame(bar, deferred.bar)
    }
}
