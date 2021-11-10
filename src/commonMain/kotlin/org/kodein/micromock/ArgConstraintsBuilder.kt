package org.kodein.micromock

import kotlin.reflect.KClass


public class ArgConstraintsBuilder internal constructor(@PublishedApi internal val mapper: ReturnMapper) {
    @PublishedApi
    internal fun <T> toReturn(constraint: ArgConstraint<T>, cls: KClass<*>): T = mapper.toReturn(constraint, cls)

    public inline fun <reified T> isAny(capture: MutableList<T>? = null): T = toReturn(ArgConstraint.isAny(capture), T::class)
    public inline fun <reified T> isEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isEqual(expected, capture), T::class)
    public inline fun <reified T> isNotEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotEqual(expected, capture), T::class)
    public inline fun <reified T> isSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isSame(expected, capture), T::class)
    public inline fun <reified T> isNotSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotSame(expected, capture), T::class)
    public inline fun <reified T> isNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNull(capture), T::class)
    public inline fun <reified T> isNotNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotNull(capture), T::class)

    public inline fun <reified T> isValid(constraint: ArgConstraint<T>): T = toReturn<T>(constraint, T::class)
    @Suppress("UNCHECKED_CAST")
    public inline fun <reified T> isValid(capture: MutableList<T>? = null, noinline test: (T) -> ArgConstraint.Result): T = isValid<T>(ArgConstraint(capture, test as (Any?) -> ArgConstraint.Result))
}
