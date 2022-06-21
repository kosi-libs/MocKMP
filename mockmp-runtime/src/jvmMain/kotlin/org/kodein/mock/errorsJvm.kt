package org.kodein.mock


public actual class MockerVerificationLazyAssertionError
@PublishedApi internal actual constructor(messageBuilder: () -> String)
: MockerVerificationAssertionError(null)
{
    override val message: String get() = lazyMessage
    public actual val lazyMessage: String by lazy(messageBuilder)
}
