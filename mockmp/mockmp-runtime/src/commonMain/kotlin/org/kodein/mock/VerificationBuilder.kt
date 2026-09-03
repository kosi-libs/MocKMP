package org.kodein.mock

import kotlin.jvm.JvmName


/**
 * The receiver of a [Mocker.verify] / [Mocker.verifyWithSuspend] block.
 *
 * It is an [ArgConstraintsBuilder] — so every constraint (`isAny`, `isEqual`, custom `isValid`…) is
 * available to describe the expected calls — plus [threw] and [called] for verifying a call whose
 * mocked behaviour did not return normally.
 */
public class VerificationBuilder internal constructor(references: References) : ArgConstraintsBuilder(references) {

    /**
     * Verifies a call whose mocked behaviour threw, without constraining the exception type.
     *
     * @return The [Throwable] that was thrown, to assert on further.
     * @throws MockerVerificationAssertionError if the call was not made, or was made without throwing.
     */
    @JvmName("threwAny")
    public inline fun threw(block: () -> Unit): Throwable = threw<Throwable>(block)

    /**
     * Verifies a call whose mocked behaviour threw a [E].
     *
     * Required for any call that was mocked to throw — a plain verification of such a call fails
     * with [MockerVerificationThrownAssertionError].
     *
     * @return The thrown exception, to assert on further.
     * @throws MockerVerificationAssertionError if the call was not made, was made without throwing,
     * or threw something that is not a [E].
     */
    public inline fun <reified E : Throwable> threw(block: () -> Unit): E {
        try {
            block()
        } catch (ex: MockerVerificationThrownAssertionError) {
            val cause = ex.cause
            if (cause !is E) throw MockerVerificationLazyAssertionError { "Expected ${E::class.simpleName} exception to be thrown, but was ${cause::class.simpleName}" }
            return cause
        }
        throw MockerVerificationLazyAssertionError { "No exception was thrown" }
    }

    /**
     * Verifies a call whose mocked behaviour may or may not have thrown.
     *
     * @return A [Result] holding the call's return value, or the exception it threw.
     * @throws MockerVerificationAssertionError if the call was not made.
     */
    public inline fun <T> called(block: () -> T): Result<T> = runCatching(block)

}
