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
