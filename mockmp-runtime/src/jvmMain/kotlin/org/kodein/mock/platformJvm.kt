package org.kodein.mock

import org.objenesis.ObjenesisStd
import kotlin.reflect.KClass


@Suppress("USELESS_CAST")
private object UnsafeFunctions {
    val f0 = ({ })
            as () -> Unit
    val f1 = ({ _: Nothing -> })
            as (Nothing) -> Unit
    val f2 = ({ _: Nothing, _: Nothing -> })
            as (Nothing, Nothing) -> Unit
    val f3 = ({ _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing) -> Unit
    val f4 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing, Nothing) -> Unit
    val f5 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
    val f6 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
    val f7 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
    val f8 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
            as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
    val f9 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
    val f10 = ({ _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing, _: Nothing -> })
                as (Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing) -> Unit
}

@Suppress("UNCHECKED_CAST")
internal actual fun <T> KClass<*>.unsafeValue() = when (this) {
    Function0::class -> UnsafeFunctions.f0
    Function1::class -> UnsafeFunctions.f1
    Function2::class -> UnsafeFunctions.f2
    Function3::class -> UnsafeFunctions.f3
    Function4::class -> UnsafeFunctions.f4
    Function5::class -> UnsafeFunctions.f5
    Function6::class -> UnsafeFunctions.f6
    Function7::class -> UnsafeFunctions.f7
    Function8::class -> UnsafeFunctions.f8
    Function9::class -> UnsafeFunctions.f9
    Function10::class -> UnsafeFunctions.f10
    else -> {
        val target = if (this.java.isInterface) {
            // Objenesis cannot instantiate an interface, so we try to find a generated Mock based on the conventional naming.
            // If the Mock class exists, we can use it to generate an instance.
            try {
                this.java.classLoader.loadClass(this.java.packageName + ".Mock" + this.java.simpleName)
            } catch (e: ClassNotFoundException) {
                error(
                    "Cannot instantiate the class ${this.qualifiedName}. " +
                            "Please try to use @UsesMock on this interface to generate a mock or report the issue."
                )
            }
        } else {
            java
        }
        ObjenesisStd(true).newInstance(target)
    }
} as T

@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
