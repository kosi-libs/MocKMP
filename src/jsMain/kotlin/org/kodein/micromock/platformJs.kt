package org.kodein.micromock

import kotlin.reflect.KClass


internal actual class ReturnMapper actual constructor() {

    internal actual fun <T> toReturn(constraint: ArgConstraint, cls: KClass<*>): T = constraint.unsafeCast<T>()

    internal actual fun toProvided(from: Any): Any = from

}
