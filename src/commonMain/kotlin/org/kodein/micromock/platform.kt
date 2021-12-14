package org.kodein.micromock

import kotlin.reflect.KClass


internal expect fun <T> KClass<*>.unsafeValue(): T

@PublishedApi
internal expect fun KClass<*>.bestName(): String
