package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal expect fun KClass<*>.bestName(): String

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect open class RuntimeNoSTException(message: String?) : Exception
