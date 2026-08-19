package tests

import foo.CardGame
import foo.Suit
import org.kodein.mock.Mock
import org.kodein.mock.generated.injectMocks
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

/**
 * Regression test: [Suit] is only ever referenced as a nullable parameter (see [CardGame]), so the
 * processor decides it needs no fake/placeholder — but `isAny()` still needs one to build its
 * constraint, since it cannot tell, from an erased `KClass<Suit>`, whether the parameter it was called
 * for is `Suit` or `Suit?`. See [CardGame] for the full explanation.
 */
class NullableParameterIsAnyTests : TestsWithMocks() {

    override fun setUpMocks() = mocker.injectMocks(this)

    @Mock
    lateinit var game: CardGame

    @Test
    fun isAnyWorksOnAParameterWhoseTypeIsOnlyEverReferencedNullably() {
        every { game.play(isAny()) } returns Unit
        game.play(Suit.SPADES)
        verify { game.play(Suit.SPADES) }
    }

    // The parameter's nullability itself was never in question — null was always a valid placeholder
    // for it. This confirms the fix (seeding a placeholder for the underlying non-null Suit) didn't
    // regress the nullable path it left untouched.
    @Test
    fun playStillAcceptsNull() {
        every { game.play(isAny()) } returns Unit
        game.play(null)
        verify { game.play(isNull()) }
    }
}
