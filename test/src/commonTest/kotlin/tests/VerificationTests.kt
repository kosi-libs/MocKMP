package tests

import foo.Bar
import foo.Foo
import foo.MockFoo
import org.kodein.micromock.Mocker
import org.kodein.micromock.UsesMocks
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@UsesMocks(Foo::class)
class VerificationTests {

    val mocker = Mocker()

    @BeforeTest
    fun setUp() {
        mocker.reset()
    }

    @Test
    fun testSimpleRun() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit

        foo.doInt(42)

        mocker.verify {
            foo.doInt(42)
        }
    }

    @Test
    fun testExhaustive() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()

        val ex = assertFailsWith<AssertionError> {
            mocker.verify {
                foo.doInt(42)
            }
        }
        assertEquals("Expected call list to be empty, but got a call to MockFoo.newString()", ex.message)
    }

    @Test
    fun testUnexpectedCall() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit

        foo.doInt(42)

        val ex = assertFailsWith<AssertionError> {
            mocker.verify {
                foo.newString()
            }
        }
        assertEquals("Expected a call to MockFoo.newString(), but was a call to MockFoo.doInt(kotlin.Int)", ex.message)
    }

    @Test
    fun testNonExhaustive() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()

        mocker.verify(exhaustive = false) {
            foo.doInt(42)
        }
    }

    @Test
    fun testNonExhaustiveUnexpectedCall() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()

        val ex = assertFailsWith<AssertionError> {
            mocker.verify(exhaustive = false) {
                foo.newInt()
            }
        }
        assertEquals("Could not find a call to MockFoo.newInt()", ex.message)
    }

    @Test
    fun testNonOrdered() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()
        foo.doInt(21)

        mocker.verify(inOrder = false) {
            foo.newString()
            foo.doInt(21)
            foo.doInt(42)
        }
    }

    @Test
    fun testNonOrderedUnexpectedCall() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()
        foo.doInt(21)

        val ex = assertFailsWith<AssertionError> {
            mocker.verify(inOrder = false) {
                foo.doInt(63)
            }
        }
        assertEquals("Found 2 calls to MockFoo.doInt(kotlin.Int), but none that validates the constraints", ex.message)
    }

    @Test
    fun testNonOrderedExhaustive() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()

        val ex = assertFailsWith<AssertionError> {
            mocker.verify(inOrder = false) {
                foo.newString()
            }
        }
        assertEquals("Expected call list to be empty, but got a call to MockFoo.doInt(kotlin.Int)", ex.message)
    }

    @Test
    fun testNonExhaustiveOrdered() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInt(isAny()) } returns Unit
        mocker.every { foo.newString() } returns ""

        foo.doInt(42)
        foo.newString()
        foo.doInt(21)

        val ex = assertFailsWith<AssertionError> {
            mocker.verify(exhaustive = false) {
                foo.newString()
                foo.doInt(42)
            }
        }
        assertEquals("Argument 1: Expected <42>, actual <21>", ex.message)
    }
}
