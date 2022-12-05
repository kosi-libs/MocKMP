package org.kodein.mock.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver


internal fun String.withNonEmptyPrefix(p: String) = if (isEmpty()) "" else "$p$this"

internal fun KSClassDeclaration.firstPublicConstructor() = (sequenceOf(primaryConstructor) + getConstructors())
    .filterNotNull()
    .filter { it.isPublic() }
    .sortedBy { it.parameters.size }
    .firstOrNull()


internal fun KSTypeReference.toRealTypeName(typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY): TypeName {

    val type = resolve()
    val decl = type.declaration

    if (decl is KSTypeAlias) {
        return decl.type.toRealTypeName(decl.typeParameters.toTypeParameterResolver(typeParamResolver))
    }

    return toTypeName(typeParamResolver)
}

internal fun TypeName.qualified(): String =
    when (this) {
        is ClassName -> canonicalName
        is ParameterizedTypeName -> rawType.canonicalName
        else -> error("Unsupported type: $this")
    }

internal fun KSName.isKotlinStdlib() = asString().let { it == "kotlin" || it.startsWith("kotlin.") }
