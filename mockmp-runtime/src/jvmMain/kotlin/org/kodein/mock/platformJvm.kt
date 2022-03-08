package org.kodein.mock

import javassist.util.proxy.ProxyFactory
import org.objenesis.ObjenesisStd
import java.lang.reflect.Proxy
import java.lang.reflect.Array
import java.lang.reflect.Modifier
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
internal actual fun <T> References.unsafeValue(cls: KClass<*>): T = when {
    cls.java.isInterface -> Proxy.newProxyInstance(cls.java.classLoader, arrayOf(cls.java)) { _, m, _ -> error(m.toString()) }
    cls.java.isArray -> Array.newInstance(cls.java.componentType, 0)
    Modifier.isAbstract(cls.java.modifiers) -> {
        val constructor = cls.java.constructors.first()
        constructor.parameterTypes
        val parameterValues = Array(constructor.parameterCount) { getReference(constructor.parameterTypes[it].kotlin) }
        ProxyFactory().apply {
            superclass = cls.java
            isUseCache = true
        }.create(constructor.parameterTypes, parameterValues)
    }
    else -> ObjenesisStd(true).newInstance(cls.java)
} as T

@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
