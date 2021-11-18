package tests

import foo.Bar
import foo.Foo
import foo.MockBar
import foo.MockFoo
import org.kodein.micromock.Mocker
import org.kodein.micromock.UsesMocks
import kotlin.test.*


@UsesMocks(Foo::class, Bar::class)
class BehaviourTests {

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
    }

    @Test
    fun lambdaCaptureAtDefinition() {
        val bar = MockBar(mocker)
        val captures = ArrayList<(String) -> Int>()
        mocker.every { bar.callback(isAny(captures)) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun lambdaCaptureAtVerification() {
        val bar = MockBar(mocker)
        mocker.every { bar.callback(isAny()) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        val captures = ArrayList<(String) -> Int>()
        mocker.verify { bar.callback(isAny(captures)) }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun changeBehaviour() {
        val foo = MockFoo<Bar>(mocker)
        val onNewInt = mocker.every { foo.newInt() }
        onNewInt returns 21
        assertEquals(21, foo.newInt())
        onNewInt returns 42
        assertEquals(42, foo.newInt())
    }

}
