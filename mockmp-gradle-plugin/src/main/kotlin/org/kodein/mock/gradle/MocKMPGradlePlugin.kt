package org.kodein.mock.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest


// The entire purpose of this Gradle plugin is to get around https://github.com/google/ksp/issues/567
// This issue is scheduled to be fixed in KSP 1.1 (according to milestone defined in the issue).
// Come on Google, having KSP work in multiplatform correctly must be very important!
class MocKMPGradlePlugin : Plugin<Project> {

    @Suppress("EnumEntryName")
    enum class SourceSetTarget {
        CommonMain,
        CommonTest_Via_JVM
    }

    @Suppress("PropertyName")
    class Extension(
        var usesHelper: Boolean = false,
        var throwErrors: Boolean = false,
        var public: Boolean = false,
        var targetSourceSet: SourceSetTarget = SourceSetTarget.CommonTest_Via_JVM
    ) {
        val CommonMain get() = SourceSetTarget.CommonMain
        val CommonTest_Via_JVM get() = SourceSetTarget.CommonTest_Via_JVM
    }

    private val Project.kotlinExtension get() = project.extensions.findByType<KotlinMultiplatformExtension>() ?: throw GradleException("Could not find Kotlin/Multiplatform plugin")

    private fun addKSPDependency(project: Project, kspConfiguration: String) {
        project.dependencies {
            kspConfiguration("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
        }
    }

    private fun addRuntimeDependencies(project: Project, sourceSet: KotlinSourceSet, ext: Extension) {
        sourceSet.dependencies {
            implementation("org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
            if (ext.usesHelper) {
                implementation(project.provider {
                    val isJunit5 = project.tasks.withType<KotlinJvmTest>().any { it.testFramework is JUnitPlatformTestFramework }
                    if (isJunit5) "org.kodein.mock:mockmp-test-helper-junit5:${BuildConfig.VERSION}"
                    else "org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}"
                })
            }
        }
    }

    private fun configureKsp(project: Project, ext: Extension) {
        val ksp = project.extensions.getByName<com.google.devtools.ksp.gradle.KspExtension>("ksp")
        if (ext.throwErrors) {
            ksp.arg("org.kodein.mock.errors", "throw")
        }
        if (ext.public) {
            ksp.arg("org.kodein.mock.visibility", "public")
        }
    }

    private fun executeOnTests(project: Project, ext: Extension) {
        val kotlin = project.kotlinExtension

        val jvmTarget = kotlin.targets.firstOrNull { it.preset?.name == "jvm" }
            ?: kotlin.targets.firstOrNull { it.preset?.name == "android" }
            ?: throw GradleException("Could not find JVM or Android target")

        when (jvmTarget.preset!!.name) {
            "jvm" -> addKSPDependency(project, "ksp${jvmTarget.name.replaceFirstChar { it.titlecase() }}Test")
            "android" -> addKSPDependency(project, "ksp${jvmTarget.name.replaceFirstChar { it.titlecase() }}TestDebug")
        }

        project.afterEvaluate {
            val commonTest = kotlin.sourceSets.getByName("commonTest")

            addRuntimeDependencies(project, commonTest, ext)

            // Adding KSP JVM result to COMMON source set
            when (jvmTarget.preset!!.name) {
                "jvm" -> commonTest.kotlin.srcDir("${project.buildDir}/generated/ksp/${jvmTarget.name}/${jvmTarget.name}Test/kotlin")
                "android" -> commonTest.kotlin.srcDir("${project.buildDir}/generated/ksp/${jvmTarget.name}/${jvmTarget.name}DebugUnitTest/kotlin")
            }

            configureKsp(project, ext)

            // Adding KSP JVM as a dependency to all Kotlin compilations
            project.tasks.withType<KotlinCompile<*>>().all {
                if (name.startsWith("compile") && name.contains("TestKotlin")) {
                    when (jvmTarget.preset!!.name) {
                        "jvm" -> dependsOn("kspTestKotlin${jvmTarget.name.replaceFirstChar { it.titlecase() }}")
                        "android" -> dependsOn("kspDebugUnitTestKotlin${jvmTarget.name.replaceFirstChar { it.titlecase() }}")
                    }
                }
            }
        }
    }

    private fun executeOnMain(project: Project, ext: Extension) {
        addKSPDependency(project, "kspCommonMainMetadata")
        addRuntimeDependencies(project, project.kotlinExtension.sourceSets["commonMain"], ext)
        configureKsp(project, ext)
    }

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension()
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            when (ext.targetSourceSet) {
                SourceSetTarget.CommonMain -> executeOnMain(target, ext)
                SourceSetTarget.CommonTest_Via_JVM -> executeOnTests(target, ext)
            }

        }
    }

}
