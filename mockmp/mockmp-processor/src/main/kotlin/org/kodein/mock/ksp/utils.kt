package org.kodein.mock.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*


internal fun String.withNonEmptyPrefix(p: String) = if (isEmpty()) "" else "$p$this"

internal fun String.withNonEmptySuffix(s: String) = if (isEmpty()) "" else "$this$s"

internal fun KSClassDeclaration.firstPublicConstructor() = (sequenceOf(primaryConstructor) + getConstructors())
    .filterNotNull()
    .filter { it.isPublic() }
    .sortedBy { it.parameters.size }
    .firstOrNull()


internal fun KSName.isKotlinStdlib() = asString().let { it == "kotlin" || it.startsWith("kotlin.") }
