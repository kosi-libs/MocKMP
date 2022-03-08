package org.kodein.mock.ksp


internal fun String.withNonEmptyPrefix(p: String) = if (isEmpty()) "" else "$p$this"
