package org.kodein.micromock

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo


class MicroMockProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private companion object {
        val mockerTypeName = ClassName("org.kodein.micromock", "Mocker")
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val toInject = HashMap<KSClassDeclaration, ArrayList<KSPropertyDeclaration>>()
        val toMock = HashMap<KSClassDeclaration, ArrayList<KSFile>>()

        fun addToMock(decl: KSDeclaration, file: KSFile) {
            if (decl !is KSClassDeclaration || decl.classKind != ClassKind.INTERFACE) error("Cannot generate mock for non interface type $decl")
            toMock.getOrPut(decl) { ArrayList() } .add(file)
        }

        val fields = resolver.getSymbolsWithAnnotation("org.kodein.micromock.Mock")
        fields.forEach { vSet ->
            if (vSet !is KSPropertySetter) error("$vSet is not a property setter but is annotated with @Mock")
            val vProp = vSet.receiver
            val vCls = vProp.parentDeclaration as? KSClassDeclaration ?: error("Cannot generate injector for $vSet as it is not inside a class")
            toInject.getOrPut(vCls) { ArrayList() } .add(vProp)
            addToMock(vProp.type.resolve().declaration, vCls.containingFile!!)
        }

        val users = resolver.getSymbolsWithAnnotation("org.kodein.micromock.UsesMocks")
        users.forEach { vUser ->
            val vAnno = vUser.annotations.first { it.annotationType.resolve().declaration.qualifiedName!!.asString() == "org.kodein.micromock.UsesMocks" }
            (vAnno.arguments.first().value as List<*>).forEach {
                addToMock((it as KSType).declaration, vUser.containingFile!!)
            }
        }

        toMock.forEach { (vItf, files) ->
            val mockedClassName = "Mocked${vItf.simpleName.asString()}"
            val gFile = FileSpec.builder(vItf.packageName.asString(), mockedClassName)
            val gCls = TypeSpec.classBuilder(mockedClassName)
                .addSuperinterface(vItf.toClassName())
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("mocker", mockerTypeName)
                        .build()
                )
            val mocker = PropertySpec.builder("mocker", mockerTypeName)
                .initializer("mocker")
                .addModifiers(KModifier.PRIVATE)
                .build()
            gCls.addProperty(mocker)
            vItf.getAllFunctions()
                .filter { it.isAbstract }
                .forEach { vFun ->
                    val gFun = FunSpec.builder(vFun.simpleName.asString())
                        .returns(vFun.returnType!!.toTypeName())
                        .addModifiers(KModifier.OVERRIDE)
                    vFun.parameters.forEach { vParam ->
                        gFun.addParameter(vParam.name!!.asString(), vParam.type.toTypeName())
                    }
                    val paramsDescription = vFun.parameters.joinToString { (it.type.resolve().declaration as? KSClassDeclaration)?.qualifiedName?.asString() ?: "?" }
                    val paramsCall = if (vFun.parameters.isEmpty()) "" else vFun.parameters.joinToString(prefix = ", ") { it.name!!.asString() }
                    gFun.addStatement("return this.%N.register(this, %S$paramsCall)", mocker, "${vFun.simpleName.asString()}($paramsDescription)")
                    gCls.addFunction(gFun.build())
                }
            gFile.addType(gCls.build())
            gFile.build().writeTo(codeGenerator, Dependencies(false, *files.toTypedArray()))
        }

        toInject.forEach { (vCls, vProps) ->
            val gFile = FileSpec.builder(vCls.packageName.asString(), "${vCls.simpleName.asString()}_injectMocks")
            val gFun = FunSpec.builder("injectMocks")
                .receiver(vCls.toClassName())
                .addParameter("mocker", mockerTypeName)
            vProps.forEach { vProp ->
                gFun.addStatement(
                    "this.%N = %T(%N)",
                    vProp.simpleName.asString(),
                    vProp.type.resolve().declaration.let { ClassName(it.packageName.asString(), "Mocked${it.simpleName.asString()}") },
                    "mocker"
                )
            }
            gFile.addFunction(gFun.build())
            gFile.build().writeTo(codeGenerator, Dependencies(false, vCls.containingFile!!))
        }

        return emptyList()
    }
}
