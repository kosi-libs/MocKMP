package tests

import foo.SDeps
import foo.SSwapped
import org.kodein.mock.Fake
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SealedFakeTests : TestsWithMocks() {

    @Fake
    lateinit var swapped: SSwapped<String, Int>

    @Fake
    lateinit var deps: SDeps

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testSubclassTypeArguments() {
        // SSwapped<String, Int> means Impl<X = Int, Y = String>, not Impl<String, Int>.
        val impl = assertIs<SSwapped.Impl<Int, String>>(swapped)
        assertEquals(0, impl.x)
        assertEquals("", impl.y)
    }

    @Test
    fun testSubclassConstructorDependency() {
        val impl = assertIs<SDeps.Impl>(deps)
        assertEquals("", impl.dep.s)
    }
}
