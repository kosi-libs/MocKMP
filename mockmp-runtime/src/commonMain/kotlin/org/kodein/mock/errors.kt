package org.kodein.mock


public open class MockerVerificationAssertionError @PublishedApi internal constructor(message: String?) : AssertionError(message)

// Needed because of https://youtrack.jetbrains.com/issue/KT-43490
public expect class MockerVerificationLazyAssertionError
@PublishedApi internal constructor(messageBuilder: () -> String)
: MockerVerificationAssertionError {
    public val lazyMessage: String
}

public class MockerVerificationThrownAssertionError internal constructor(override val cause: Throwable, methodName: () -> String)
    : MockerVerificationAssertionError("${methodName()} was called but threw an exception. You should verify it with threw {}.")
