package org.kodein.micromock

import kotlin.reflect.KClass


// This works while this issue is unresolved:
// https://youtrack.jetbrains.com/issue/KT-40613
@Suppress("UNCHECKED_CAST")
private fun <T> Any?.unsafeCast(): T = this as T

private object UnsafeValue

internal actual fun <T> KClass<*>.unsafeValue() = UnsafeValue.unsafeCast<T>()

@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
