package org.kodein.micromock

import kotlin.reflect.KClass


public class ArgConstraintsBuilder internal constructor(@PublishedApi internal val mapper: ReturnMapper) {
    @PublishedApi
    internal fun <T> toReturn(constraint: ArgConstraint, cls: KClass<*>): T = mapper.toReturn(constraint, cls)

    public inline fun <reified T> isAny(): T = toReturn(ArgConstraint.isAny(), T::class)
    public inline fun <reified T : Any> isEqual(expected: T): T = toReturn<T>(ArgConstraint.isEqual(expected), T::class)
    public inline fun <reified T : Any> isNotEqual(expected: T): T = toReturn<T>(ArgConstraint.isNotEqual(expected), T::class)
    public inline fun <reified T : Any> isSame(expected: T): T = toReturn<T>(ArgConstraint.isSame(expected), T::class)
    public inline fun <reified T : Any> isNotSame(expected: T): T = toReturn<T>(ArgConstraint.isNotSame(expected), T::class)
    public inline fun <reified T> isNull(): T = toReturn<T>(ArgConstraint.isNull(), T::class)
    public inline fun <reified T> isNotNull(): T = toReturn<T>(ArgConstraint.isNotNull(), T::class)
}
