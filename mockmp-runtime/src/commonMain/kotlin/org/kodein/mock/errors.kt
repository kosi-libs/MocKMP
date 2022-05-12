package org.kodein.mock


public open class MockerVerificationAssertionError @PublishedApi internal constructor(messageBuilder: () -> String) : AssertionError() {
    override val message: String by lazy(messageBuilder)
}

public class MockerVerificationThrownAssertionError internal constructor(override val cause: Throwable, methodName: () -> String)
    : MockerVerificationAssertionError({ "${methodName()} was called but threw an exception. You should verify it with threw {}." })
