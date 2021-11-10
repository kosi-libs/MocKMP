package org.kodein.micromock

import kotlin.reflect.KClass


internal actual class ReturnMapper actual constructor() {

    // This works while this issue is unresolved:
    // https://youtrack.jetbrains.com/issue/KT-40613
    @Suppress("UNCHECKED_CAST")
    private fun <T> Any?.unsafeCast(): T = this as T

    private val providedMap = HashMap<Any, ArgConstraint<*>>()

    private var byteCounter: UByte = 0u
    private var shortCounter: UShort = 0u
    private var intCounter: UInt = 0u
    private var longCounter: ULong = 0u

    internal actual fun <T> toReturn(constraint: ArgConstraint<*>, cls: KClass<*>): T {
        val ret = when (cls) {
            UByte::class -> byteCounter++
            Byte::class -> byteCounter++.toByte()
            UShort::class -> shortCounter++
            Short::class -> shortCounter++.toShort()
            Char::class -> shortCounter++.toInt().toChar()
            UInt::class -> intCounter++
            Int::class -> intCounter++.toInt()
            Float::class -> intCounter++
            ULong::class -> longCounter++
            Long::class -> longCounter++.toLong()
            Double::class -> longCounter++.toDouble()
            else -> return constraint.unsafeCast()
        }
        providedMap[ret] = constraint
        @Suppress("UNCHECKED_CAST")
        return ret as T

    }

    internal actual fun toProvided(from: Any): Any = providedMap.remove(from) ?: from
}
