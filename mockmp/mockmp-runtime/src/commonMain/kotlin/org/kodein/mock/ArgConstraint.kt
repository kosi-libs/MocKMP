package org.kodein.mock

import kotlin.reflect.KClass


/**
 * A reusable check on a single mocked-call argument.
 *
 * [test] decides whether an argument matches, [description] names the constraint in failure
 * messages, and [capture], when given, collects every argument the constraint matched.
 *
 * This is the extension point behind custom constraints: build one and hand it to
 * [ArgConstraintsBuilder.isValid] to use it in a definition or verification block. The companion
 * object holds the constraints behind the built-in `ArgConstraintsBuilder.isAny` / `isEqual` / …
 * functions, ready to be composed into a custom one.
 *
 * @param T The argument type the constraint checks.
 * @param capture A list every matched argument is appended to, or `null` to capture nothing.
 * @param description Builds the constraint's name for failure messages.
 * @param test Returns [Result.Success] when the argument matches, [Result.Failure] otherwise.
 */
public class ArgConstraint<T>(
    internal val capture: MutableList<T>? = null,
    internal val description: () -> String = { "?" },
    internal val test: (T) -> Result,
) {

    /** The outcome of testing an argument against an [ArgConstraint]. */
    public sealed class Result {
        /** The argument matched the constraint. */
        public object Success : Result()
        /**
         * The argument did not match.
         *
         * @property error Builds the reason, on demand, for inclusion in the failure message.
         */
        public class Failure(public val error: () -> String) : Result()
    }

    /**
     * The constraint objects behind the identically-named [ArgConstraintsBuilder] functions.
     *
     * Use these to compose a custom [ArgConstraint]; in a definition or verification block, call
     * the [ArgConstraintsBuilder] functions instead.
     */
    public companion object {
        private fun result(success: Boolean, error: () -> String) = if (success) Result.Success else Result.Failure(error)

        /** Matches any argument, `null` included. See [ArgConstraintsBuilder.isAny]. */
        public fun <T> isAny(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isAny" }) { Result.Success }
        /** Matches an argument equal to [expected] (`==`). See [ArgConstraintsBuilder.isEqual]. */
        public fun <T> isEqual(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isEqual($expected)" }) { actual -> result(actual == expected) { "Expected <$expected>, actual <$actual>" } }
        /** Matches an argument not equal to [expected] (`!=`). See [ArgConstraintsBuilder.isNotEqual]. */
        public fun <T> isNotEqual(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isNotEqual($expected)" }) { actual -> result(actual != expected) { "Illegal value: <$actual>" } }
        /** Matches the very instance [expected] (`===`). See [ArgConstraintsBuilder.isSame]. */
        public fun <T> isSame(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isSame($expected)" }) { actual -> result(actual === expected) { "Expected <$expected>, actual <$actual> is not same" } }
        /** Matches any instance other than [expected] (`!==`). See [ArgConstraintsBuilder.isNotSame]. */
        public fun <T> isNotSame(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isNotSame($expected)" }) { actual -> result(actual !== expected) { "Expected not same as <$actual>" } }
        /** Matches a `null` argument. See [ArgConstraintsBuilder.isNull]. */
        public fun <T> isNull(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isNull" }) { actual -> result(actual == null) { "Expected value to be null, but was: <$actual>" } }
        /** Matches a non-`null` argument. See [ArgConstraintsBuilder.isNotNull]. */
        public fun <T> isNotNull(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isNotNull"} ) { actual -> result(actual != null) { "Expected value to be not null" } }
        /** Matches an argument that is an instance of [cls]. See [ArgConstraintsBuilder.isInstanceOf]. */
        public fun <T> isInstanceOf(cls: KClass<*>, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, { "isInstanceOf<${cls.simpleName}>" }) { actual -> result(cls.isInstance(actual)) { "Expected an instance of type ${cls.simpleName}, but was <$actual>" } }
    }
}


internal fun <T> ArgConstraint<T>.isValid(arg: T): Boolean = test(arg) is ArgConstraint.Result.Success

internal fun <T> ArgConstraint<T>.assert(name: String, arg: T) { (test(arg) as? ArgConstraint.Result.Failure)?.let { throw MockerVerificationLazyAssertionError { "$name: ${it.error()}" } } }
