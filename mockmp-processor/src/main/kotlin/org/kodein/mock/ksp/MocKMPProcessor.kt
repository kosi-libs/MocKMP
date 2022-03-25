package org.kodein.mock.ksp

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*


public class MocKMPProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val throwErrors: Boolean
) : SymbolProcessor {

    private companion object {
        val mockerTypeName = ClassName("org.kodein.mock", "Mocker")
        val builtins = mapOf(
            "kotlin.Unit" to ("%L" to "Unit"),
            "kotlin.Boolean" to ("%L" to "false"),
            "kotlin.Byte" to ("%L" to "0"),
            "kotlin.Short" to ("%L" to "0"),
            "kotlin.Int" to ("%L" to "0"),
            "kotlin.Long" to ("%L" to "0L"),
            "kotlin.Float" to ("%L" to "0f"),
            "kotlin.Double" to ("%L" to "0.0"),
            "kotlin.String" to ("%L" to "\"\""),
            "kotlin.collections.List" to ("%M()" to MemberName("kotlin.collections", "emptyList")),
            "kotlin.collections.Set" to ("%M()" to MemberName("kotlin.collections", "emptySet")),
            "kotlin.collections.Map" to ("%M()" to MemberName("kotlin.collections", "emptyMap")),
        )
    }

    private val KSType.isAnyFunctionType get() = isFunctionType || isSuspendFunctionType

    private class Error(message: String, val node: KSNode) : Exception(message)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        try {
            return privateProcess(resolver)
        } catch (e: Error) {
            logger.error("MocKMP: ${e.message}", e.node)
            if (throwErrors) throw e
            return emptyList()
        }
    }

    private fun KSNode.asString(): String = when (this) {
        is KSValueParameter -> {
            val nameStr = name?.asString() ?: "?"
            if (parent != null) "${parent!!.asString()}($nameStr)"
            else nameStr
        }
        is KSClassDeclaration -> this.qualifiedName?.asString() ?: this.simpleName.asString()
        else -> (parent?.let { it.asString() + "." } ?: "") + toString()
    }

    @Suppress("NOTHING_TO_INLINE")
    private fun error(node: KSNode, message: String): Nothing {
        val prefix = when (val loc = node.location) {
            is FileLocation -> "$node (${loc.filePath}:${loc.lineNumber})"
            is NonExistLocation -> node.asString()
        }
        throw Error("$prefix: $message", node)
    }

    private class ToProcess {
        val files = HashSet<KSFile>()
        val references = HashSet<KSNode>()
    }

    @OptIn(KotlinPoetKspPreview::class)
    private fun privateProcess(resolver: Resolver): List<KSAnnotated> {
        val toInject = HashMap<KSClassDeclaration, ArrayList<Pair<String, KSPropertyDeclaration>>>()
        val toMock = HashMap<KSClassDeclaration, ToProcess>()
        val toFake = HashMap<KSClassDeclaration, ToProcess>()

        fun addMock(type: KSType, files: Iterable<KSFile>, node: KSNode) {
            if (type.isFunctionType) return
            val decl = type.declaration
            if (decl !is KSClassDeclaration || decl.classKind != ClassKind.INTERFACE) error(node, "Cannot generate mock for non interface $decl")
            toMock.getOrPut(decl) { ToProcess() } .let {
                it.files.addAll(files)
                it.references.add(node)
            }
        }

        fun addFake(type: KSType, files: Iterable<KSFile>, node: KSNode) {
            val decl = type.declaration
            if (
                decl !is KSClassDeclaration || decl.isAbstract() ||
                decl.classKind !in arrayOf(ClassKind.CLASS, ClassKind.ENUM_CLASS)
            ) {
                error(node, "Cannot generate fake for non concrete class ${decl.qualifiedName?.asString() ?: decl.simpleName.asString()}")
            }
            toFake.getOrPut(decl) { ToProcess() } .let {
                it.files.addAll(files)
                it.references.add(node)
            }
        }

        fun lookUpFields(annotationName: String, add: (KSType, Iterable<KSFile>, KSNode) -> Unit) {
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            symbols.forEach { symbol ->
                val prop = when (symbol) {
                    is KSPropertySetter -> symbol.receiver
                    is KSPropertyDeclaration -> {
                        if (!symbol.isMutable) error(symbol, "$symbol is immutable but is annotated with @${annotationName.split(".").last()}")
                        symbol
                    }
                    else -> error(symbol, "$symbol is not a property nor a property setter but is annotated with @${annotationName.split(".").last()} (is ${symbol::class.simpleName})")
                }
                val cls = prop.parentDeclaration as? KSClassDeclaration ?: error(symbol, "Cannot generate injector for $prop as it is not inside a class")
                toInject.getOrPut(cls) { ArrayList() } .add(annotationName to prop)
                add(prop.type.resolve(), listOf(cls.containingFile!!), symbol)
            }
        }

        fun lookUpUses(annotationName: String, add: (KSType, Iterable<KSFile>, KSNode) -> Unit) {
            val uses = resolver.getSymbolsWithAnnotation(annotationName)
            uses.forEach { use ->
                val anno = use.annotations.first { it.annotationType.resolve().declaration.qualifiedName!!.asString() == annotationName }
                (anno.arguments.first().value as List<*>).forEach {
                    add((it as KSType), listOf(use.containingFile!!), use)
                }
            }
        }

        lookUpFields("org.kodein.mock.Mock", ::addMock)
        lookUpFields("org.kodein.mock.Fake", ::addFake)
        lookUpUses("org.kodein.mock.UsesMocks", ::addMock)
        lookUpUses("org.kodein.mock.UsesFakes", ::addFake)

        val providedFakes = run {
            val provided = HashMap<KSClassDeclaration, KSFunctionDeclaration>()
            resolver.getSymbolsWithAnnotation("org.kodein.mock.FakeProvider").forEach {
                if (it !is KSFunctionDeclaration || it.parent !is KSFile) error(it, "Only top-level functions can be annotated with @FakeProvider")
                val typeDeclaration = it.returnType?.resolve()?.declaration
                if (typeDeclaration !is KSClassDeclaration) error(it, "@FakeProvider functions must return class types.")
                if (typeDeclaration in provided) error(it, "Only one @FakeProvider function must exist for this type (other is ${provided[typeDeclaration]!!.asString()}).")
                toFake.remove(typeDeclaration)
                provided[typeDeclaration] = it
            }
            provided
        }

        run {
            val toExplore = ArrayDeque(toFake.map { it.toPair() })
            while (toExplore.isNotEmpty()) {
                val (cls, process) = toExplore.removeFirst()
                cls.firstPublicConstructor()?.parameters?.forEach { param ->
                    if (!param.hasDefault) {
                        val paramType = param.type.resolve()
                        if (paramType.nullability == Nullability.NOT_NULL) {
                            val fakeType = if (paramType.isAnyFunctionType) paramType.arguments.last().type!!.resolve() else paramType
                            val fakeDecl = fakeType.declaration
                            if (fakeDecl.qualifiedName!!.asString() !in builtins && fakeDecl !in providedFakes && fakeDecl !in toFake) {
                                addFake(fakeType, process.files, param)
                                toExplore.add((fakeDecl as KSClassDeclaration) to process)
                            }
                        }
                    }
                }
            }
        }

        toMock.forEach { (vItf, process) ->
            val mockClassName = "Mock${vItf.simpleName.asString()}"
            val gFile = FileSpec.builder(vItf.packageName.asString(), mockClassName)
            val gCls = TypeSpec.classBuilder(mockClassName)
                .addSuperinterface(
                    if (vItf.typeParameters.isEmpty()) vItf.toClassName()
                    else vItf.toClassName().parameterizedBy(vItf.typeParameters.map { it.toTypeVariableName() })
                )
                .addModifiers(KModifier.INTERNAL)
            vItf.typeParameters.forEach { vParam ->
                gCls.addTypeVariable(vParam.toTypeVariableName())
            }
            gCls.primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("mocker", mockerTypeName)
                        .build()
                )
            val mocker = PropertySpec.builder("mocker", mockerTypeName)
                .initializer("mocker")
                .addModifiers(KModifier.PRIVATE)
                .build()
            gCls.addProperty(mocker)
            vItf.getAllProperties()
                .filter { it.isAbstract() }
                .forEach { vProp ->
                    val typeParamResolver = vItf.typeParameters.toTypeParameterResolver()
                    val gProp = PropertySpec.builder(vProp.simpleName.asString(), vProp.type.toTypeName(typeParamResolver))
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return this.%N.register(this, %S)", mocker, "get:${vProp.simpleName.asString()}")
                                .build()
                        )
                    if (vProp.isMutable) {
                        gProp.mutable(true)
                            .setter(
                                FunSpec.setterBuilder()
                                    .addParameter("value", vProp.type.toTypeName(typeParamResolver))
                                    .addStatement("return this.%N.register(this, %S, value)", mocker, "set:${vProp.simpleName.asString()}")
                                    .build()
                            )
                    }
                    gCls.addProperty(gProp.build())
                }
            vItf.getAllFunctions()
                .filter { it.simpleName.asString() !in listOf("equals", "hashCode") }
                .forEach { vFun ->
                    val gFun = FunSpec.builder(vFun.simpleName.asString())
                        .addModifiers(KModifier.OVERRIDE)
                    val typeParamResolver = vFun.typeParameters.toTypeParameterResolver(vItf.typeParameters.toTypeParameterResolver())
                    vFun.typeParameters.forEach { vParam ->
                        gFun.addTypeVariable(vParam.toTypeVariableName(typeParamResolver))
                    }
                    gFun.addModifiers((vFun.modifiers - Modifier.ABSTRACT - Modifier.OPEN - Modifier.OPERATOR).mapNotNull { it.toKModifier() })
                    gFun.returns(vFun.returnType!!.toTypeName(typeParamResolver))
                    vFun.parameters.forEach { vParam ->
                        gFun.addParameter(vParam.name!!.asString(), vParam.type.toTypeName(typeParamResolver))
                    }
                    val paramsDescription = vFun.parameters.joinToString { (it.type.resolve().declaration as? KSClassDeclaration)?.qualifiedName?.asString() ?: "?" }
                    val paramsCall = if (vFun.parameters.isEmpty()) "" else vFun.parameters.joinToString { it.name!!.asString() }
                    val register = if (Modifier.SUSPEND in vFun.modifiers) "registerSuspend" else "register"
                    val default = if (vFun.isAbstract) "" else "default = { super.${vFun.simpleName.asString()}($paramsCall) }"
                    gFun.addStatement(
                        "return this.%N.$register(this, %S${paramsCall.withNonEmptyPrefix(", ")}${default.withNonEmptyPrefix(", ")})",
                        mocker,
                        "${vFun.simpleName.asString()}($paramsDescription)"
                    )
                    gCls.addFunction(gFun.build())
                }
            gFile.addType(gCls.build())
            gFile.build().writeTo(codeGenerator, Dependencies(true, *process.files.toTypedArray()))
        }

        toFake.forEach { (vCls, process) ->
            val filesDeps = HashSet(process.files)
            val mockFunName = "fake${vCls.simpleName.asString()}"
            val gFile = FileSpec.builder(vCls.packageName.asString(), mockFunName)
            val gFun = FunSpec.builder(mockFunName)
                .addModifiers(KModifier.INTERNAL)
            when (vCls.classKind) {
                ClassKind.CLASS -> {
                    val vCstr = vCls.firstPublicConstructor()
                        ?: error(vCls, "Could not find public constructor for ${vCls.qualifiedName?.asString()}. Please create a @FakeProvider for it.")
                    val args = ArrayList<Pair<String, Any>>()
                    vCstr.parameters.forEach { vParam ->
                        if (!vParam.hasDefault) {
                            val vParamType = vParam.type.resolve()
                            if (vParamType.nullability != Nullability.NOT_NULL) {
                                args.add("${vParam.name!!.asString()} = %L" to "null")
                            } else {
                                val vParamTypeDecl = if (vParamType.isAnyFunctionType) vParamType.arguments.last().type!!.resolve().declaration else vParamType.declaration
                                val builtIn = builtins[vParamTypeDecl.qualifiedName!!.asString()]
                                val (template, value) = when {
                                    builtIn != null -> builtIn
                                    vParamTypeDecl in providedFakes -> {
                                        val f = providedFakes[vParamTypeDecl]!!
                                        f.containingFile?.let { filesDeps += it }
                                        "%M()" to MemberName(f.packageName.asString(), f.simpleName.asString())
                                    }
                                    else -> "%M()" to MemberName(vParamTypeDecl.packageName.asString(), "fake${vParamTypeDecl.simpleName.asString()}")
                                }
                                if (vParamType.isAnyFunctionType) {
                                    args.add("${vParam.name!!.asString()} = { ${"_, ".repeat(vParamType.arguments.size - 1)}-> $template }" to value)
                                } else {
                                    args.add("${vParam.name!!.asString()} = $template" to value)
                                }
                            }
                        }
                    }
                    gFun.addStatement("return %T(${args.joinToString { it.first }})", *(listOf(vCls.toClassName()) + args.map { it.second }).toTypedArray())
                }
                ClassKind.ENUM_CLASS -> {
                    val firstEntry = vCls.declarations.filterIsInstance<KSClassDeclaration>().firstOrNull { it.classKind == ClassKind.ENUM_ENTRY }
                        ?: error(vCls, "Cannot fake empty enum class ${vCls.qualifiedName!!.asString()}")
                    gFun.addStatement("return %T.%L", vCls.toClassName(), firstEntry.simpleName.asString())
                }
                else -> error(vCls, "Cannot process ${vCls.classKind}")
            }
            gFile.addFunction(gFun.build())
            gFile.build().writeTo(codeGenerator, Dependencies(true, *filesDeps.toTypedArray()))
        }

        toInject.forEach { (vCls, vProps) ->
            val filesDeps = HashSet<KSFile>().apply { vCls.containingFile?.let { add(it) } }

            val gFile = FileSpec.builder(vCls.packageName.asString(), "${vCls.simpleName.asString()}_injectMocks")
            val gFun = FunSpec.builder("injectMocks")
                .addModifiers(KModifier.INTERNAL)
                .receiver(vCls.toClassName())
                .addParameter("mocker", mockerTypeName)
            vProps.forEach { (anno, vProp) ->
                val vPropType = vProp.type.resolve()
                val vPropTypeDecl = vPropType.declaration
                when {
                    anno == "org.kodein.mock.Mock" -> {
                        if (vPropType.isFunctionType) {
                            val argCount = vPropType.arguments.size - 1
                            val args =
                                if (argCount == 0) ""
                                else vPropType.arguments.take(argCount).joinToString(prefix = ", ") { "\"${it.type!!.resolve().declaration.qualifiedName!!.asString()}\"" }
                            gFun.addStatement(
                                "this.%N = %M(%N$args)",
                                vProp.simpleName.asString(),
                                MemberName("org.kodein.mock", "mockFunction$argCount"),
                                "mocker"
                            )
                        } else {
                            gFun.addStatement(
                                "this.%N = %T(%N)",
                                vProp.simpleName.asString(),
                                ClassName(vPropTypeDecl.packageName.asString(), "Mock${vPropTypeDecl.simpleName.asString()}"),
                                "mocker"
                            )
                        }
                    }
                    anno == "org.kodein.mock.Fake" -> {
                        val builtIn = builtins[vPropTypeDecl.qualifiedName!!.asString()]
                        val (template, value) = when {
                            builtIn != null -> builtIn
                            vPropTypeDecl in providedFakes -> {
                                val f = providedFakes[vPropTypeDecl]!!
                                f.containingFile?.let { filesDeps += it }
                                "%M()" to MemberName(f.packageName.asString(), f.simpleName.asString())
                            }
                            else -> "%M()" to MemberName(vPropTypeDecl.packageName.asString(), "fake${vPropTypeDecl.simpleName.asString()}")
                        }
                        gFun.addStatement(
                            "this.%N = $template",
                            vProp.simpleName.asString(),
                            value
                        )
                    }
                }
            }
            gFile.addFunction(gFun.build())
            gFile.build().writeTo(codeGenerator, Dependencies(true, *filesDeps.toTypedArray()))
        }

        return emptyList()
    }
}
