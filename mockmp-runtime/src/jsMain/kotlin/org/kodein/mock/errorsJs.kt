package org.kodein.mock


public actual class MockerVerificationLazyAssertionError
@PublishedApi internal actual constructor(private val messageBuilder: () -> String)
: MockerVerificationAssertionError("A bug in the JS/IR compiler (KT-43490) prevents the exception message from being built. Please refer to lazyMessage.")
{
    public actual val lazyMessage: String by lazy(messageBuilder)
}
