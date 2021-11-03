package org.kodein.micromock


public fun interface ArgConstraint {
    public sealed class Result {
        public object Success : Result()
        public class Failure(public val error: () -> String) : Result()
    }
    public fun test(arg: Any?): Result

    public companion object {
        private fun result(success: Boolean, error: () -> String) = if (success) Result.Success else Result.Failure(error)

        public fun isAny(): ArgConstraint = ArgConstraint { Result.Success }
        public fun isEqual(expected: Any): ArgConstraint = ArgConstraint { actual -> result(actual == expected) { "Expected <$expected>, actual <$actual>." } }
        public fun isNotEqual(expected: Any): ArgConstraint = ArgConstraint { actual -> result(actual != expected) { "Illegal value: <$actual>." } }
        public fun isSame(expected: Any): ArgConstraint = ArgConstraint { actual -> result(actual === expected) { "Expected <$expected>, actual <$actual> is not same." } }
        public fun isNotSame(expected: Any): ArgConstraint = ArgConstraint { actual -> result(actual !== expected) { "Expected not same as <$actual>." } }
        public fun isNull(): ArgConstraint = ArgConstraint { actual -> result(actual == null) { "Expected value to be null, but was: <$actual>." } }
        public fun isNotNull(): ArgConstraint = ArgConstraint { actual -> result(actual != null) { "Expected value to be not null." } }
    }
}

public fun ArgConstraint.isValid(arg: Any?): Boolean = test(arg) is ArgConstraint.Result.Success

public fun ArgConstraint.assert(arg: Any?) { (test(arg) as? ArgConstraint.Result.Failure)?.let { throw AssertionError(it.error()) } }
