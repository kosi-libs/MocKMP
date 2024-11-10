package org.kodein.mock

import javassist.util.proxy.ProxyFactory
import org.objenesis.ObjenesisStd
import java.lang.reflect.Proxy
import java.lang.reflect.Array
import java.lang.reflect.Modifier
import kotlin.reflect.KClass


private val hasSealed by lazy {
    Class::class.java.methods.any { it.name == "isSealed" }
}

internal actual fun References.unsafeValue(cls: KClass<*>): Any? {
    when {
        cls.java.isArray -> return Array.newInstance(cls.java.componentType, 0)

        hasSealed && cls.isSealed -> {
            cls.sealedSubclasses.forEach { subCls ->
                tryGetReference(subCls)?.let { return it }
            }
            return null
        }

        cls.java.isInterface -> return Proxy.newProxyInstance(cls.java.classLoader, arrayOf(cls.java)) { _, m, _ -> error(m.toString()) }

        Modifier.isAbstract(cls.java.modifiers) -> {
            val constructor = cls.java.constructors.first()
            val parameterValues = Array(constructor.parameterCount) { getReference(constructor.parameterTypes[it].kotlin) }
            return ProxyFactory().apply {
                superclass = cls.java
                isUseCache = true
            }.create(constructor.parameterTypes, parameterValues)
        }

        cls.java.isAnnotationPresent(JvmInline::class.java) -> {
            val constructor = cls.java.declaredConstructors[0]
            val parameter = getReference(constructor.parameterTypes[0].kotlin)
            constructor.isAccessible = true
            try {
                return constructor.newInstance(parameter)
            } finally {
                constructor.isAccessible = false
            }
        }

        else -> return ObjenesisStd(true).newInstance(cls.java)
    }
}

@PublishedApi
internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: "Unknown"
