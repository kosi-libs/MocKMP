package org.kodein.mock

import kotlin.jvm.JvmName


public class VerificationBuilder internal constructor(references: References) : ArgConstraintsBuilder(references) {

    @JvmName("threwAny")
    public inline fun threw(block: () -> Unit): Throwable = threw<Throwable>(block)

    public inline fun <reified E : Throwable> threw(block: () -> Unit): E {
        try {
            block()
        } catch (ex: MockerVerificationThrownAssertionError) {
            val cause = ex.cause
            if (cause !is E) throw MockerVerificationAssertionError { "Expected ${E::class.simpleName} exception to be thrown, but was ${cause::class.simpleName}" }
            return cause
        }
        throw MockerVerificationAssertionError { "No exception was thrown" }
    }

    public inline fun <T> called(block: () -> T): Result<T> = runCatching(block)

}
