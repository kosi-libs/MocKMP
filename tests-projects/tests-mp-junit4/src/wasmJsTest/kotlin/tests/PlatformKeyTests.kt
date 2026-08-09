package tests

import foo.Bar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.mock.ArgConstraint
import org.kodein.mock.Mocker
import org.kodein.mock.MockerVerificationAssertionError
import org.kodein.mock.generated.mock
import org.kodein.mock.mockFunction1
import org.kodein.mock.mockSuspendFunction1
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * `bestName()` is the one expect/actual in the runtime, and this is the JS/Wasm half of pinning it:
 * `simpleName`, so a reified `mockFunctionN(mocker)` keys on `invoke(String)`. The JVM/Native half
 * lives in the matching source sets under `src/jvmTest` and `src/nativeTest`, asserting
 * `invoke(kotlin.String)`.
 *
 * Only the *reified* overloads go through `bestName()`. A mock the processor generates is handed an
 * explicit `a1Type = "kotlin.String"`, which is why its key is identical everywhere -- see
 * `InjectionTests.testCallbackOfOneArgumentRegistrationKey`, which asserts exactly that.
 */

class PlatformKeyTests {

    @Test
    fun theReifiedFunctionMockKeysOnTheSimpleName() {
        val mocker = Mocker()
        val cb: (String) -> Unit = mockFunction1(mocker)

        val ex = assertFailsWith<Mocker.MockingException> { cb("x") }

        assertContains(ex.message!!, "invoke(String)")
        assertFalse("invoke(kotlin.String)" in ex.message!!, ex.message)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun theReifiedSuspendFunctionMockKeysOnTheSimpleName() = runTest {
        val mocker = Mocker()
        val cb: suspend (String) -> Unit = mockSuspendFunction1(mocker)

        val ex = assertFailsWith<Mocker.MockingException> { cb("x") }

        assertContains(ex.message!!, "invoke(String)")
        assertFalse("invoke(kotlin.String)" in ex.message!!, ex.message)
    }

    // The other bestName() consumer: the type named by an isValid constraint that the argument does
    // not satisfy.
    @Test
    fun theIsValidMismatchNamesTheSimpleName() {
        val mocker = Mocker()
        val bar = mocker.mock<Bar>()
        mocker.every { bar.doAny(isAny()) } returns Unit

        bar.doAny(42)

        val ex = assertFailsWith<MockerVerificationAssertionError> {
            mocker.verify { bar.doAny(isValid<String> { ArgConstraint.Result.Success }) }
        }
        assertContains(ex.message!!, "Expected an instance of String")
    }
}
