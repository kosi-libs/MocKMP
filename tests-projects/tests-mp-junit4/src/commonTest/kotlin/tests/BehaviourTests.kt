package tests

import foo.Bar
import foo.Foo
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock
import org.kodein.mock.mockFunction0
import kotlin.test.*


@UsesMocks(Foo::class, Bar::class)
class BehaviourTests {

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
    }

    @Test
    fun testLambdaCaptureAtDefinition() {
        val bar = mocker.mock<Bar>()
        val captures = ArrayList<(String) -> Int>()
        mocker.every { bar.callback(isAny(captures)) } returns Unit

        var lambdaValue: String? = null
        bar.callback { lambdaValue = it ; 42 }

        assertNull(lambdaValue)
        captures.single().invoke("Some String")
        assertEquals("Some String", lambdaValue)
    }

    @Test
    fun testLambdaCaptureAtVerification() {
        val bar = mocker.mock<Bar>()
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
    fun testChangeBehaviour() {
        val foo = mocker.mock<Foo<Bar>>()
        val onNewInt = mocker.every { foo.newInt() }
        onNewInt returns 21
        assertEquals(21, foo.newInt())
        onNewInt returns 42
        assertEquals(42, foo.newInt())
    }

    @Test
    fun testThrows() {
        val bar = mocker.mock<Bar>()
        mocker.every { bar.doNothing() } runs { error("This is a test!") }

        val ex = assertFailsWith<IllegalStateException> { bar.doNothing() }
        assertEquals("This is a test!", ex.message)
    }

    @Test
    fun testReturnsNull() {
        val foo = mocker.mock<Foo<Bar>>()
        mocker.every { foo.newStringNullable() } returns null

        assertNull(foo.newStringNullable())

        mocker.verify { assertNull(foo.newStringNullable()) }
    }

    @Test
    fun testProperty() {
        val foo = mocker.mock<Foo<Bar>>()

        mocker.backProperty(foo, Foo<Bar>::rwString, "")

        assertEquals("", foo.rwString)
        foo.rwString = "Test!"
        assertEquals("Test!", foo.rwString)
    }

    @Test
    fun test() {
        val f1: () -> Int = mockFunction0(mocker) { 1 }
        val f2: () -> Int = mockFunction0(mocker) { 2 }
        assertEquals(1 , f1.invoke())
        assertEquals(2 , f2.invoke())
    }
}
