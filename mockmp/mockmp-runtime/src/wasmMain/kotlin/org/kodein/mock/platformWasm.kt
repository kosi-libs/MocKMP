package org.kodein.mock

import kotlin.reflect.KClass


private object UnsafeValue

// This works while this issue is unresolved:
// https://youtrack.jetbrains.com/issue/KT-40613
internal actual fun References.unsafeValue(cls: KClass<*>): Any? = UnsafeValue

@PublishedApi
internal actual fun KClass<*>.bestName(): String = simpleName ?: "Unknown"
