package tests

import data.Direction
import data.fakeData
import foo.*
import foo.MockBar
import foo.MockFoo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
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
    fun testInterfaceArgument() {
        val foo = MockFoo<Bar>(mocker)
        mocker.every { foo.doInterface(isAny()) } returns Unit

        foo.doInterface(MockBar(mocker))

        mocker.verify {
            foo.doInterface(isAny())
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

    // TODO: This makes JS/IR crash. Should be fixed in Kotlin 1.4.20
//    @Test
//    fun testSuspendFunctionArgument() {
//        val bar = MockBar(mocker)
//        mocker.every { bar.suspendCallback(isAny()) } returns Unit
//
//        bar.suspendCallback { 42 }
//
//        mocker.verify {
//            bar.suspendCallback(isAny())
//        }
//    }

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
}
