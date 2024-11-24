package org.kodein.mock

import kotlin.reflect.KClass


private object UnsafeValue

internal actual fun References.unsafeValue(cls: KClass<*>): Any? = UnsafeValue.unsafeCast<Any?>()

@PublishedApi
internal actual fun KClass<*>.bestName(): String = simpleName ?: "Unknown"
