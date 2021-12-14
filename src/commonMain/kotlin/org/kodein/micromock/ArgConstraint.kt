package org.kodein.micromock


public class ArgConstraint<T>(internal val capture: MutableList<T>? = null, internal val description: String = "?", internal val test: (T) -> Result) {

    public sealed class Result {
        public object Success : Result()
        public class Failure(public val error: () -> String) : Result()
    }

    public companion object {
        private fun result(success: Boolean, error: () -> String) = if (success) Result.Success else Result.Failure(error)

        public fun <T> isAny(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isAny") { Result.Success }
        public fun <T> isEqual(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isEqual($expected)") { actual -> result(actual == expected) { "Expected <$expected>, actual <$actual>" } }
        public fun <T> isNotEqual(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isNotEqual($expected)") { actual -> result(actual != expected) { "Illegal value: <$actual>" } }
        public fun <T> isSame(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isSame($expected)") { actual -> result(actual === expected) { "Expected <$expected>, actual <$actual> is not same" } }
        public fun <T> isNotSame(expected: T, capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isNotSame($expected)") { actual -> result(actual !== expected) { "Expected not same as <$actual>" } }
        public fun <T> isNull(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isNull") { actual -> result(actual == null) { "Expected value to be null, but was: <$actual>" } }
        public fun <T> isNotNull(capture: MutableList<T>? = null): ArgConstraint<T> = ArgConstraint(capture, "isNotNull") { actual -> result(actual != null) { "Expected value to be not null" } }
    }
}

internal fun <T> ArgConstraint<T>.isValid(arg: T): Boolean = test(arg) is ArgConstraint.Result.Success

internal fun <T> ArgConstraint<T>.assert(name: String, arg: T) { (test(arg) as? ArgConstraint.Result.Failure)?.let { throw AssertionError("$name: ${it.error()}") } }
