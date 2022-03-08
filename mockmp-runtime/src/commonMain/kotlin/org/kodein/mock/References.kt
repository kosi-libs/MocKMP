package org.kodein.mock

import kotlin.reflect.KClass


internal class References {

    private val references = ArrayList<Any>()

    @Suppress("RemoveRedundantCallsOfConversionMethods")
    private val map = hashMapOf<KClass<*>, Any>(
        Boolean::class to false,
        UByte::class to 0.toUByte(),
        Byte::class to 0.toByte(),
        UShort::class to 0.toUShort(),
        Short::class to 0.toShort(),
        Char::class to 0.toChar(),
        UInt::class to 0.toUInt(),
        Int::class to 0.toInt(),
        Float::class to 0.toFloat(),
        ULong::class to 0.toULong(),
        Long::class to 0.toLong(),
        Double::class to 0.toDouble()
    )

    fun addReference(r: Any) {
        references.add(r)
        map[r::class] = r
    }

    fun getReference(cls: KClass<*>): Any = map.getOrPut(cls) {
        for (ref in references) {
            if (cls.isInstance(ref)) return@getOrPut ref
        }
        unsafeValue(cls)
    }

}
