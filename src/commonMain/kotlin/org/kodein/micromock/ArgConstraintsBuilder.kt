package org.kodein.micromock

import kotlin.reflect.KClass


public class ArgConstraintsBuilder internal constructor() {
    private val constraints: MutableList<ArgConstraint<*>> = ArrayList()

    internal fun getConstraints(args: Array<*>): List<ArgConstraint<*>> {
        val list = when {
            constraints.size == args.size -> constraints.toList()
            constraints.isEmpty() -> args.map { if (it == null) ArgConstraint.isNull() else ArgConstraint.isEqual(it) }
            else -> throw Mocker.MockingException("You cannot mix literal values and constraints. Please replace all literal values by their constraint counterpart (isEqual(value) or isNull()).")
        }
        constraints.clear()
        return list
    }

    @PublishedApi
    internal fun <T> toReturn(constraint: ArgConstraint<T>, cls: KClass<*>): T {
        constraints.add(constraint)

        @Suppress("UNCHECKED_CAST", "RemoveRedundantCallsOfConversionMethods", "IMPLICIT_CAST_TO_ANY")
        return when (cls) {
            Boolean::class -> false
            UByte::class -> 0.toUByte()
            Byte::class -> 0.toByte()
            UShort::class -> 0.toUShort()
            Short::class -> 0.toShort()
            Char::class -> 0.toChar()
            UInt::class -> 0.toUInt()
            Int::class -> 0.toInt()
            Float::class -> 0.toFloat()
            ULong::class -> 0.toULong()
            Long::class -> 0.toLong()
            Double::class -> 0.toDouble()
            else -> cls.unsafeValue<T>()
        } as T
    }

    public inline fun <reified T> isAny(capture: MutableList<T>? = null): T = toReturn(ArgConstraint.isAny(capture), T::class)
    public inline fun <reified T> isEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isEqual(expected, capture), T::class)
    public inline fun <reified T> isNotEqual(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotEqual(expected, capture), T::class)
    public inline fun <reified T> isSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isSame(expected, capture), T::class)
    public inline fun <reified T> isNotSame(expected: T, capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotSame(expected, capture), T::class)
    public inline fun <reified T> isNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNull(capture), T::class)
    public inline fun <reified T> isNotNull(capture: MutableList<T>? = null): T = toReturn<T>(ArgConstraint.isNotNull(capture), T::class)

    public inline fun <reified T> isValid(constraint: ArgConstraint<T>): T = toReturn<T>(constraint, T::class)
    @Suppress("UNCHECKED_CAST")
    public inline fun <reified T> isValid(capture: MutableList<T>? = null, description: String = "isValid", noinline test: (T) -> ArgConstraint.Result): T = isValid<T>(ArgConstraint(capture, description, test as (Any?) -> ArgConstraint.Result))
}
