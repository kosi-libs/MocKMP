package org.kodein.mock

import kotlin.reflect.KClass


internal expect fun <T> References.unsafeValue(cls: KClass<*>): T

@PublishedApi
internal expect fun KClass<*>.bestName(): String
