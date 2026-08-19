package foo

// Suit is only ever referenced here, as a nullable parameter — the reproduction condition for the bug
// NullableParameterIsAnyTests exists to catch: the processor decides a fake/placeholder for a type is
// unnecessary whenever every reference to it is nullable, since a nullable value is satisfied by
// `null` alone (see MocKMPProcessor.seedImplicitPlaceholder). But isAny() (and every other
// ArgConstraintsBuilder constraint) still needs a *real*, non-null placeholder for Suit whenever it is
// asked for one: its signature is erased to a plain `KClass<Suit>` — via `T::class`, not `typeOf<T>()`
// (using `typeOf<T>()` here fails to compile wherever the same reified `T` might be inferred as a
// suspend functional type elsewhere, with "Suspend functional types are not supported in typeOf") — so
// there is no way, at that call site, to tell "T was Suit" apart from "T was Suit?", and it goes
// looking for a Suit placeholder that generation never produced.
enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

interface CardGame {
    fun play(suit: Suit?)
}
