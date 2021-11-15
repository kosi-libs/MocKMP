package org.kodein.micromock

import org.objenesis.ObjenesisStd
import java.util.*
import kotlin.reflect.KClass


internal actual class ReturnMapper actual constructor() {

    private val providedMap = IdentityHashMap<Any, ArgConstraint<*>>()

    private val objenesis = ObjenesisStd(false)

    private var byteCounter: UByte = 0u
    private var shortCounter: UShort = 0u
    private var intCounter: UInt = 0u
    private var longCounter: ULong = 0u

    @Suppress("USELESS_CAST")
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

            Function0::class -> ({ })
                    as () -> Unit
            Function1::class -> ({ _: Nothing -> })
                    as (Nothing) -> Unit
            Function2::class -> ({ _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing) -> Unit
            Function3::class -> ({ _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing) -> Unit
            Function4::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing) -> Unit
            Function5::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
            Function6::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
            Function7::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
            Function8::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
            Function9::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
            Function10::class -> ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                    as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit

            else -> objenesis.newInstance(cls.java)
        }
        providedMap[ret] = constraint
        @Suppress("UNCHECKED_CAST")
        return ret as T
    }

    internal actual fun toProvided(from: Any): Any = providedMap.remove(from) ?: from
}
