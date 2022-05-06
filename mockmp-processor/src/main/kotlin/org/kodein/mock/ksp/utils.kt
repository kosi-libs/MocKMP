package org.kodein.mock.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName


internal fun String.withNonEmptyPrefix(p: String) = if (isEmpty()) "" else "$p$this"

internal fun KSClassDeclaration.firstPublicConstructor() = (sequenceOf(primaryConstructor) + getConstructors()).firstOrNull { it?.isPublic() ?: false }


@KotlinPoetKspPreview
internal fun KSTypeReference.toRealTypeName(typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY): TypeName {

    val type = resolve()
    val decl = type.declaration

    if (decl is KSTypeAlias) {
        return decl.type.toTypeName(typeParamResolver)
    }

    return toTypeName(typeParamResolver)
}