package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal expect fun KClass<*>.bestName(): String
