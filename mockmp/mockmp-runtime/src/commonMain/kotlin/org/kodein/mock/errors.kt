package org.kodein.mock


public open class MockerVerificationAssertionError @PublishedApi internal constructor(message: String?) : AssertionError(message)

public class MockerVerificationLazyAssertionError
@PublishedApi internal constructor(messageBuilder: () -> String)
: MockerVerificationAssertionError(null) {
    public val lazyMessage: String by lazy(messageBuilder)
    override val message: String get() = lazyMessage
}

public class MockerVerificationThrownAssertionError internal constructor(override val cause: Throwable, methodName: () -> String)
    : MockerVerificationAssertionError("${methodName()} was called but threw an exception. You should verify it with threw {}.")

/**
 * Thrown by a generated `placeholderXxx()` function that the processor could not build a value
 * for — [message] is the processor's own explanation of why, and what to do about it
 * (`mocker.useReference(...)`). Public, unlike every other exception here: generated code, living
 * in the user's own module, has to construct it.
 *
 * Both [References.getReference] and [ArgConstraintsBuilder.resolvePlaceholder] rethrow this
 * unwrapped rather than folding it into their own generic "could not find a reference" message —
 * the processor already said everything there is to say, so wrapping it further would only bury
 * the actual reason under boilerplate.
 */
public class MocKMPNoPlaceholderException(message: String) : RuntimeException(message)
