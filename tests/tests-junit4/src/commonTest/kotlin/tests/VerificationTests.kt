package tests

import data.Direction
import data.fakeData
import foo.*
import foo.MockBar
import foo.MockFoo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.MockerVerificationAssertionError
import org.kodein.mock.MockerVerificationThrownAssertionError
import org.kodein.mock.UsesMocks
import kotlin.test.*


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

        val ex = assertFailsWith<MockerVerificationAssertionError> {
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

        val ex = assertFailsWith<Mocker.MockingException> {
            mocker.verify {
                foo.newString()
            }
        }
        assertEquals("Cannot verify MockFoo.newString() as it has not been mocked", ex.message)
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

        val ex = assertFailsWith<Mocker.MockingException> {
            mocker.verify(exhaustive = false) {
                foo.newInt()
            }
        }
        assertEquals("Cannot verify MockFoo.newInt() as it has not been mocked", ex.message)
    }

    @Test
    fun testOrderedMultipleMocks() {
        val foo1 = MockFoo<Bar>(mocker)
        val foo2 = MockFoo<Bar>(mocker)
        mocker.every { foo1.doInt(isAny()) } returns Unit
        mocker.every { foo1.newString() } returns ""
        mocker.every { foo2.doInt(isAny()) } returns Unit
        mocker.every { foo2.newString() } returns ""

        foo1.doInt(42)
        foo2.doInt(42)
        foo1.newString()
        foo2.newString()

        var firstHalfPassed = false
        val ex = assertFailsWith<AssertionError> {
            mocker.verify(inOrder = true) {
                foo1.doInt(42)
                foo2.doInt(42)
                firstHalfPassed = true
                // Same method but different receiver order
                foo2.newString()
                foo1.newString()
            }
        }
        assertTrue(firstHalfPassed)
        assertEquals("Got a call to MockFoo.newString(), but expected a different MockFoo receiver", ex.message)
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

        val ex = assertFailsWith<MockerVerificationAssertionError> {
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

        val ex = assertFailsWith<MockerVerificationAssertionError> {
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

        val ex = assertFailsWith<MockerVerificationAssertionError> {
            mocker.verify(exhaustive = false) {
                foo.newString()
                foo.doInt(42)
            }
        }
        assertEquals("Argument 1: Expected <42>, actual <21>", ex.message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspend() = runTest {
        val bar = MockBar(mocker)
        mocker.everySuspending { bar.newData() } returns fakeData()
        val data = bar.newData()
        assertEquals(fakeData(), data)
        mocker.verifyWithSuspend { bar.newData() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testSuspendFails() = runTest {
        val bar = MockBar(mocker)
        val ex = assertFailsWith<Mocker.MockingException> {
            mocker.verifyWithSuspend { bar.newData() }
        }
        assertEquals("Cannot verify MockBar.newData() as it has not been mocked", ex.message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testNonSuspendInSuspendingEvery() = runTest {
        val foo = MockFoo<Int>(mocker)
        val ex = assertFailsWith<IllegalStateException> {
            mocker.everySuspending { foo.defaultT } returns 42
        }
        assertEquals("Calling a non suspend function inside a suspending every block", ex.message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testNonSuspendInSuspend() = runTest {
        val foo = MockFoo<Int>(mocker)
        mocker.every { foo.defaultT } returns 42
        assertEquals(42, foo.defaultT)
        mocker.verifyWithSuspend { foo.defaultT }
    }

    @Test
    fun testAnyInterfaceArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInterface(isAny()) } returns Unit

        foo.doInterface(MockBar(mocker))

        mocker.verify {
            foo.doInterface(isAny())
        }
    }

    @Test
    fun testSpecificInterfaceArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInterface(isAny()) } returns Unit

        val bar = MockBar(mocker)
        foo.doInterface(bar)

        mocker.verify {
            foo.doInterface(isEqual(bar))
        }
    }

    @Test
    fun testSpecificInterfaceArgumentFails() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInterface(isAny()) } returns Unit

        val bar = MockBar(mocker)
        foo.doInterface(bar)

        assertFailsWith<MockerVerificationAssertionError> {
            mocker.verify {
                foo.doInterface(isEqual(MockBar(mocker)))
            }
        }
    }

    @Test
    fun testArrayArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doArray(isAny()) } returns Unit

        foo.doArray(arrayOf("Test"))

        mocker.verify {
            foo.doArray(isAny())
        }
    }

    @Test
    fun testEnumArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doEnum(isAny()) } returns Unit

        foo.doEnum(Direction.LEFT)

        mocker.verify {
            foo.doEnum(isAny())
        }
    }

    @Test
    fun testAbstractArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doAbstract(isAny()) } returns Unit

        foo.doAbstract(object : Abs(42) {})

        mocker.verify {
            foo.doAbstract(isAny())
        }
    }

    @Test
    fun testFunctionArgument() {
        val bar = MockBar(mocker)
        mocker.every { bar.callback(isAny()) } returns Unit

        bar.callback { 42 }

        mocker.verify {
            bar.callback(isAny())
        }
    }

    @Test
    fun testSuspendFunctionArgument() {
        val bar = MockBar(mocker)
        mocker.every { bar.suspendCallback(isAny()) } returns Unit

        bar.suspendCallback { 42 }

        mocker.verify {
            bar.suspendCallback(isAny())
        }
    }

    @Test
    fun testSealedClassArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doSealedClass(isAny()) } returns Unit

        foo.doSealedClass(SCls.O)

        mocker.verify {
            foo.doSealedClass(isAny())
        }
    }

    @Test
    fun testSealedInterfaceArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doSealedInterface(isAny()) } returns Unit

        foo.doSealedInterface(SItf.O)

        mocker.verify {
            foo.doSealedInterface(isAny())
        }
    }

    @Test
    fun testDefaultImplementation() {
        val bar = MockBar(mocker)
        bar.doSomething()
        mocker.verify {}
    }

    @Test
    fun testSomeDefaultImplementationOverride() {
        val bar = MockBar(mocker)
        mocker.every { bar.doNothing() } returns Unit
        bar.doSomething()
        mocker.verify {
            bar.doNothing()
        }
    }

    @Test
    fun testNoDefaultImplementationOverride() {
        val bar = MockBar(mocker)
        bar.doSomething()
        val ex = assertFailsWith<Mocker.MockingException> {
            mocker.verify {
                bar.doNothing()
            }
        }
        assertEquals("Cannot verify MockBar.doNothing() as it has not been mocked", ex.message)
    }

    @Test
    fun testAllDefaultImplementationOverride() {
        val bar = MockBar(mocker)
        mocker.every { bar.doSomething() } returns Unit
        mocker.every { bar.doNothing() } returns Unit
        bar.doSomething()
        mocker.verify {
            bar.doSomething()
        }
    }

    @Test
    fun testOverrideToString() {
        val foo = MockFoo<Bar>(mocker)

        val answers = ArrayList<Int>()
        mocker.every { foo.doInt(isAny(capture = answers)) } returns Unit
        mocker.every { foo.toString() } runs { "Answer is ${answers.last()}!" }
        foo.doInt(42)
        assertEquals("Answer is 42!", foo.toString())
        mocker.verify {
            foo.doInt(42)
            foo.toString()
        }
    }

    @Test
    fun testIsInstanceOfConstraint() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doSealedInterface(isInstanceOf<SItf.C>()) } returns Unit

        foo.doSealedInterface(SItf.C())

        mocker.verify {
            foo.doSealedInterface(isInstanceOf<SItf.C>())
        }
    }

    @Test
    fun testIsInstanceOfConstraintNotMocked() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doSealedInterface(isInstanceOf<SItf.C>()) } returns Unit

        val ex = assertFailsWith<Mocker.MockingException> {
            foo.doSealedInterface(SItf.O)
        }
        assertEquals(
            """MockFoo.doSealedInterface(foo.SItf) has not been mocked for arguments O
              |    Registered mocked:
              |        isInstanceOf<C>
            """.trimMargin(), ex.message)
    }

    @Test
    fun testIsInstanceOfConstraintNotCalled() {
        val foo = MockFoo<Bar>(mocker)
        var ran = ""
        mocker.every { foo.doSealedInterface(isInstanceOf<SItf.C>()) } runs { ran = "C" }
        mocker.every { foo.doSealedInterface(isInstanceOf<SItf.O>()) } runs { ran = "O" }

        foo.doSealedInterface(SItf.C())
        assertEquals("C", ran)

        val ex = assertFailsWith<MockerVerificationAssertionError> {
            mocker.verify {
                foo.doSealedInterface(isInstanceOf<SItf.O>())
            }
        }
        assertEquals("Argument 1: Expected an instance of type O, but was <C>", ex.message)
    }

    @Test
    fun testThrowsAndVerifyWithoutThrew() {
        val bar = MockBar(mocker)
        mocker.every { bar.doNothing() } runs { error("This is a test!") }

        assertFails { bar.doNothing() }

        mocker.verify {
            val ex = assertFailsWith<MockerVerificationThrownAssertionError> { bar.doNothing() }
            assertEquals("MockBar.doNothing() was called but threw an exception. You should verify it with threw {}.", ex.message)
        }
    }

    @Test
    fun testThrowsAndVerifyWithThrew() {
        val bar = MockBar(mocker)
        mocker.every { bar.doNothing() } runs { error("This is a test!") }

        assertFails { bar.doNothing() }

        mocker.verify {
            val ex = threw<IllegalStateException> { bar.doNothing() }
            assertEquals("This is a test!", ex.message)
        }
    }

    @Test
    fun testThrowsAndVerifyWithWrongThrew() {
        val bar = MockBar(mocker)
        mocker.every { bar.doNothing() } runs { error("This is a test!") }

        assertFails { bar.doNothing() }

        val ex = assertFailsWith<MockerVerificationAssertionError> {
            mocker.verify {
                threw<IllegalArgumentException> { bar.doNothing() }
            }
        }
        assertEquals("Expected IllegalArgumentException exception to be thrown, but was IllegalStateException", ex.message)
    }

    @Test
    fun testPropertyVerification() {
        val foo = MockFoo<Bar>(mocker)

        mocker.backProperty(foo, Foo<Bar>::rwString, "")

        assertEquals("", foo.rwString)
        foo.rwString = "Test!"
        assertEquals("Test!", foo.rwString)

        mocker.verify {
            foo.rwString
            foo.rwString = "Test!"
            foo.rwString
        }
    }

    @Test
    fun testPropertyWrong() {
        val foo = MockFoo<Bar>(mocker)

        mocker.backProperty(foo, Foo<Bar>::rwString, "")

        foo.rwString = "Test!"

        mocker.verify {
            assertFailsWith<MockerVerificationAssertionError> { foo.rwString = "Ouch!" }
        }
    }

}
