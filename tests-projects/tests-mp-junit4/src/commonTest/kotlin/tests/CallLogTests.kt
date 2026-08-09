package tests

import foo.Bar
import org.kodein.mock.Mock
import org.kodein.mock.MockerVerificationAssertionError
import org.kodein.mock.MockerVerificationThrownAssertionError
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** `called {}` and `clearCalls()`, neither of which appeared anywhere in the suite. */
class CallLogTests : TestsWithMocks() {

    override fun setUpMocks() = mocker.injectMocks(this)

    @Mock lateinit var bar: Bar

    // `called` is runCatching around the call, so it accepts a call that returned *and* one that
    // threw — that tolerance is its entire reason to exist, since `threw {}` accepts only the latter
    // and a bare call only the former. Both cases are here; either alone would leave the tolerance
    // untested.

    @Test
    fun calledAcceptsACallThatReturned() {
        every { bar.doNothing() } returns Unit
        bar.doNothing()
        verify {
            val result = called { bar.doNothing() }
            assertTrue(result.isSuccess, "expected a success, got $result")
        }
    }

    @Test
    fun calledAcceptsACallThatThrew() {
        every { bar.doNothing() } runs { error("This is a test!") }
        assertFails { bar.doNothing() }
        verify {
            val result = called { bar.doNothing() }
            assertTrue(result.isFailure, "expected a failure, got $result")
            val thrown = assertIs<MockerVerificationThrownAssertionError>(result.exceptionOrNull())
            val cause = assertIs<IllegalStateException>(thrown.cause)
            assertEquals("This is a test!", cause.message)
        }
    }

    // A bare call is the strict counterpart: the same throwing call, not wrapped in `called`, is what
    // fails. This is what makes the test above about `called` rather than about the mock.
    @Test
    fun aBareCallDoesNotTolerateAThrow() {
        every { bar.doNothing() } runs { error("This is a test!") }
        assertFails { bar.doNothing() }
        verify {
            assertFailsWith<MockerVerificationThrownAssertionError> { bar.doNothing() }
        }
    }

    // clearCalls drops the log without touching the definitions, so the later call still runs and an
    // exhaustive verify sees only it.
    @Test
    fun clearCallsDropsEarlierCalls() {
        every { bar.doNothing() } returns Unit
        every { bar.doInt(isAny()) } returns Unit

        bar.doNothing()
        mocker.clearCalls()
        bar.doInt(42)

        verify { bar.doInt(42) }
    }

    // The companion: without the clear, the same exhaustive verify fails on the leftover call. Without
    // this, the test above would pass even if clearCalls did nothing, since verify consumes in order.
    @Test
    fun withoutClearCallsTheEarlierCallIsStillVerified() {
        every { bar.doNothing() } returns Unit
        every { bar.doInt(isAny()) } returns Unit

        bar.doNothing()
        bar.doInt(42)

        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doInt(42) }
        }
        assertEquals("Expected a call to MockBar.doInt(kotlin.Int), but was a call to MockBar.doNothing()", ex.message)
    }

    // Definitions survive the clear: only the call log is dropped.
    @Test
    fun clearCallsKeepsDefinitions() {
        every { bar.newString() } returns "defined"
        assertEquals("defined", bar.newString())
        mocker.clearCalls()
        assertEquals("defined", bar.newString())
        verify { bar.newString() }
    }
}
