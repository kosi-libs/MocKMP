package org.kodein.mock

import kotlin.reflect.KClass


internal expect fun References.unsafeValue(cls: KClass<*>): Any?

@PublishedApi
internal expect fun KClass<*>.bestName(): String
