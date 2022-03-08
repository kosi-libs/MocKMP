package org.kodein.mock

import org.objenesis.ObjenesisStd
import java.lang.reflect.Proxy
import java.lang.reflect.Array
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
internal actual fun <T> KClass<*>.unsafeValue() = when {
    java.isInterface -> Proxy.newProxyInstance(java.classLoader, arrayOf(java)) { _, m, _ -> error(m.toString()) }
    java.isArray -> Array.newInstance(java.componentType, 0)
    else -> ObjenesisStd(true).newInstance(java)
} as T

@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
