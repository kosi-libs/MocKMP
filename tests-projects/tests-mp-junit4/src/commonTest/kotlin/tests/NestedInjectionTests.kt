package tests

import foo.Bar
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.generated.injectMocks
import kotlin.test.Test
import kotlin.test.assertNotNull

// Two same-simple-named nested classes in the same package, each holding @Mock properties.
class HolderA {
    class Mocks {
        @Mock
        lateinit var bar: Bar
    }
}

class HolderB {
    class Mocks {
        @Mock
        lateinit var bar: Bar
    }
}

class NestedInjectionTests {

    @Test
    fun testSameSimpleNameInjectors() {
        val mocker = Mocker()

        val a = HolderA.Mocks()
        mocker.injectMocks(a)
        assertNotNull(a.bar)

        val b = HolderB.Mocks()
        mocker.injectMocks(b)
        assertNotNull(b.bar)
    }
}
