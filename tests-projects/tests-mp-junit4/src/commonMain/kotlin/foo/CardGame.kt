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
@Deprecated("for test")
enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

// AgeRestriction is reached only as PlayerConfig's own abstract member type — never as a mocked
// interface's own property/parameter, and never as a constructor parameter (PlayerConfig is an
// interface, with no constructor to walk) — so nothing should ever be generated for it at all: a
// Placeholder's abstract members throw rather than being faked, which is what keeps
// PlaceholderPlayerConfig.ageRestriction from needing one.
interface AgeRestriction {
    val minAge: Int
    val reason: String
}

// Reached only as CardGame's own (mocked) abstract property type, so it needs a Placeholder — not a
// Mock (nothing requests one directly) and not a Fake (a Fake would recurse into ageRestriction,
// which is exactly the abstract-member transitivity a Placeholder must not have).
interface PlayerConfig {
    var playerCount: Int
    var ageRestriction: AgeRestriction
}

@Deprecated("for test")
interface CardGame {
    val config: PlayerConfig
    fun play(@Suppress("DEPRECATION") suit: Suit?)
}
