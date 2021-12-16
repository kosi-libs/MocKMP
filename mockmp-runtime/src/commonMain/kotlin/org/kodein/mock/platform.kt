package org.kodein.mock

import kotlin.reflect.KClass


internal expect fun <T> KClass<*>.unsafeValue(): T

@PublishedApi
internal expect fun KClass<*>.bestName(): String
