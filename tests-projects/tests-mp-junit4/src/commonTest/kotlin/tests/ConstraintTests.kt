package tests

import data.SomeDirection
import foo.Bar
import org.kodein.mock.Fake
import org.kodein.mock.Mock
import org.kodein.mock.MockerVerificationAssertionError
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * The argument constraints that nothing exercised: isSame, isNotSame, isNotEqual, isNull and
 * isNotNull. Each gets a matching case that passes `verify` and a non-matching one asserting the exact
 * message from `ArgConstraint`, so a constraint that silently stops constraining is caught.
 *
 * The identity ones are deliberately tested against [equalButDistinct] rather than two unrelated
 * values. Comparing something to itself satisfies isSame *and* isEqual, so such a test would pass even
 * if isSame were implemented as `==` — it would assert nothing. A value that is `==` but not `===`
 * makes the two disagree, which is the only way round to pin which is which.
 */
class ConstraintTests : TestsWithMocks() {

    override fun setUpMocks() = mocker.injectMocks(this)

    @Mock lateinit var bar: Bar

    @Fake lateinit var dir: SomeDirection

    private val equalButDistinct: SomeDirection get() = dir.copy()

    @Test
    fun theFixtureIsEqualButDistinct() {
        assertEquals(dir, equalButDistinct)
        assertNotSame(dir, equalButDistinct)
    }

    // region isSame / isNotSame

    @Test
    fun isSameMatchesTheSameInstance() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        verify { bar.doAny(isSame(dir)) }
    }

    @Test
    fun isSameRejectsAnEqualButDistinctInstance() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        val other = equalButDistinct
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doAny(isSame(other)) }
        }
        assertEquals("Argument 1: Expected <$other>, actual <$dir> is not same", ex.message)
    }

    // isSame rejects what isEqual accepts: this is the pair that makes both tests meaningful.
    @Test
    fun isEqualAcceptsWhatIsSameRejects() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        verify { bar.doAny(isEqual(equalButDistinct)) }
    }

    @Test
    fun isNotSameMatchesAnEqualButDistinctInstance() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        verify { bar.doAny(isNotSame(equalButDistinct)) }
    }

    @Test
    fun isNotSameRejectsTheSameInstance() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doAny(isNotSame(dir)) }
        }
        assertEquals("Argument 1: Expected not same as <$dir>", ex.message)
    }

    // ...and the mirror image: isNotSame accepts what isNotEqual rejects.
    @Test
    fun isNotEqualRejectsWhatIsNotSameAccepts() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        val other = equalButDistinct
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doAny(isNotEqual(other)) }
        }
        assertEquals("Argument 1: Illegal value: <$dir>", ex.message)
    }

    // endregion

    // region isNotEqual

    @Test
    fun isNotEqualMatchesADifferentValue() {
        every { bar.doPrimitive(isAny(), isAny()) } returns Unit
        bar.doPrimitive("actual", 1)
        verify { bar.doPrimitive(isNotEqual("something else"), isAny()) }
    }

    @Test
    fun isNotEqualRejectsAnEqualValue() {
        every { bar.doPrimitive(isAny(), isAny()) } returns Unit
        bar.doPrimitive("actual", 1)
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doPrimitive(isNotEqual("actual"), isAny()) }
        }
        assertEquals("Argument 1: Illegal value: <actual>", ex.message)
    }

    // endregion

    // region isNull / isNotNull

    @Test
    fun isNullMatchesNull() {
        every { bar.doNullable(isAny()) } returns Unit
        bar.doNullable(null)
        verify { bar.doNullable(isNull()) }
    }

    @Test
    fun isNullRejectsAValue() {
        every { bar.doNullable(isAny()) } returns Unit
        bar.doNullable("present")
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doNullable(isNull()) }
        }
        assertEquals("Argument 1: Expected value to be null, but was: <present>", ex.message)
    }

    @Test
    fun isNotNullMatchesAValue() {
        every { bar.doNullable(isAny()) } returns Unit
        bar.doNullable("present")
        verify { bar.doNullable(isNotNull()) }
    }

    @Test
    fun isNotNullRejectsNull() {
        every { bar.doNullable(isAny()) } returns Unit
        bar.doNullable(null)
        val ex = assertFailsWith<MockerVerificationAssertionError> {
            verify { bar.doNullable(isNotNull()) }
        }
        assertEquals("Argument 1: Expected value to be not null", ex.message)
    }

    // endregion

    // Constraints capture into a list wherever they are used, not only in isAny: the capture
    // parameter is on every one of them.
    @Test
    fun aConstraintCanCapture() {
        every { bar.doNullable(isAny()) } returns Unit
        bar.doNullable("captured")
        val captures = ArrayList<String?>()
        verify { bar.doNullable(isNotNull(capture = captures)) }
        assertEquals(listOf<String?>("captured"), captures.toList())
    }

    @Test
    fun isSameCanCapture() {
        every { bar.doAny(isAny()) } returns Unit
        bar.doAny(dir)
        val captures = ArrayList<SomeDirection>()
        verify { bar.doAny(isSame(dir, capture = captures)) }
        assertSame(dir, captures.single())
    }
}
