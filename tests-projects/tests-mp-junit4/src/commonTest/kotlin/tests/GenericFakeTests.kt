package tests

import data.GenData
import data.Wrap
import org.kodein.mock.Fake
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericFakeTests : TestsWithMocks() {

    @Fake
    lateinit var wrapString: Wrap<String>

    @Fake
    lateinit var wrapInt: Wrap<Int>

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testNestedGeneric() {
        assertEquals("", wrapString.direct)
        assertEquals(GenData("", 0), wrapString.inner)
        assertEquals(GenData(GenData("", 0), 0), wrapString.nested)
        assertEquals(GenData("", 0), wrapString.maker())
    }

    @Test
    fun testNestedGenericOtherInstantiation() {
        assertEquals(0, wrapInt.direct)
        assertEquals(GenData(0, 0), wrapInt.inner)
        assertEquals(GenData(GenData(0, 0), 0), wrapInt.nested)
        assertEquals(GenData(0, 0), wrapInt.maker())
    }
}
