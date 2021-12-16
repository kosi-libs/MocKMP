package org.kodein.mock

import kotlin.reflect.KClass


private object UnsafeValue

internal actual fun <T> KClass<*>.unsafeValue() = UnsafeValue.unsafeCast<T>()

@PublishedApi
internal actual fun KClass<*>.bestName(): String = simpleName ?: "Unknown"
