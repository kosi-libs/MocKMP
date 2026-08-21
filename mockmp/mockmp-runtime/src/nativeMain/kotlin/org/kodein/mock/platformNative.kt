package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual typealias RuntimeNoSTException = RuntimeException
