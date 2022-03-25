package org.kodein.mock.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration


internal fun String.withNonEmptyPrefix(p: String) = if (isEmpty()) "" else "$p$this"

internal fun KSClassDeclaration.firstPublicConstructor() = (sequenceOf(primaryConstructor) + getConstructors()).firstOrNull { it?.isPublic() ?: false }