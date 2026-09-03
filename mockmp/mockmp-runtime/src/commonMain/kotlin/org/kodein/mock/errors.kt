package org.kodein.mock


/**
 * Base type of every assertion failure raised from a [Mocker.verify] / [Mocker.verifyWithSuspend]
 * block: an expected call that did not happen, a call that happened but was not verified (when
 * `exhaustive`), an out-of-order call (when `inOrder`), an argument that did not match its
 * constraint, or a verified call whose behaviour threw.
 */
public open class MockerVerificationAssertionError @PublishedApi internal constructor(message: String?) : AssertionError(message)

/**
 * A [MockerVerificationAssertionError] whose message is only built when it is read.
 *
 * Verification composes many candidate failure messages while searching for a matching call; making
 * each one lazy keeps that search from formatting messages that are never shown.
 *
 * @property lazyMessage The failure message, computed on first access.
 */
public class MockerVerificationLazyAssertionError
@PublishedApi internal constructor(messageBuilder: () -> String)
: MockerVerificationAssertionError(null) {
    public val lazyMessage: String by lazy(messageBuilder)
    override val message: String get() = lazyMessage
}

/**
 * Raised when a call being verified had been mocked to throw. Verify such a call with
 * [VerificationBuilder.threw] (or [VerificationBuilder.called] if it only sometimes throws).
 *
 * @property cause The exception the mocked behaviour threw.
 */
public class MockerVerificationThrownAssertionError internal constructor(override val cause: Throwable, methodName: () -> String)
    : MockerVerificationAssertionError("${methodName()} was called but threw an exception. You should verify it with threw {}.")

/**
 * Thrown by a generated `placeholderXxx()` function that the processor could not build a value
 * for — [message] is the processor's own explanation of why, and what to do about it
 * (`mocker.useReference(...)`). Public, unlike every other exception here: generated code, living
 * in the user's own module, has to construct it.
 *
 * Both [References.getReference] and [ArgConstraintsBuilder.toReturn] rethrow this
 * unwrapped rather than folding it into their own generic "could not find a reference" message —
 * the processor already said everything there is to say, so wrapping it further would only bury
 * the actual reason under boilerplate.
 */
public class MocKMPNoPlaceholderException(message: String) : RuntimeException(message)
