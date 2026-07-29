package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
