package tests

import data.Data
import foo.Bar
import foo.Foo
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertNotNull

abstract class BaseInjectionTests : TestsWithMocks() {

    @Mock
    lateinit var bar: Bar

    @Fake
    lateinit var data: Data
}

class SubInjectionTests : BaseInjectionTests() {

    @Mock
    lateinit var s1: Foo.Sub

    override fun setUpMocks() = mocker.injectMocks(this)

    @Test
    fun testInheritedInjection() {
        assertNotNull(bar)
        assertNotNull(data)
        assertNotNull(s1)
    }

    @Test
    fun testInheritedMockBehaviour() {
        every { bar.doData(isAny()) } returns Unit
        bar.doData(data)
        verify { bar.doData(data) }
    }
}
