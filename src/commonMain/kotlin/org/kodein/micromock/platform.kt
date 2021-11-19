package org.kodein.micromock

import kotlin.reflect.KClass


internal expect class ReturnMapper() {

    internal fun <T> toReturn(constraint: ArgConstraint<*>, cls: KClass<*>): T

    internal fun toProvided(from: Any): Any

}

@PublishedApi
internal expect fun KClass<*>.bestName(): String
