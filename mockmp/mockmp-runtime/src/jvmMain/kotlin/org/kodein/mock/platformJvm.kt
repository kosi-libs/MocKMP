package org.kodein.mock

import kotlin.reflect.KClass


@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual open class RuntimeNoSTException actual constructor(message: String?) : java.lang.RuntimeException(message) {
    override fun fillInStackTrace(): Throwable? = this
    override fun getStackTrace(): Array<out StackTraceElement?>? = null
}
