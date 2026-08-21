package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal actual fun KClass<*>.bestName(): String = simpleName ?: "Unknown"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual typealias RuntimeNoSTException = RuntimeException
