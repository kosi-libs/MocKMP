package org.kodein.mock.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


// The entire purpose of this Gradle plugin is to get around https://github.com/google/ksp/issues/567
// This issue is scheduled to be fixed in KSP 1.1 (according to milestone defined in the issue).
// Come on Google, having KSP work in multiplatform correctly must be very important!
class MocKMPGradlePlugin : Plugin<Project> {

    @Suppress("PropertyName")
    class Extension(
        var usesHelper: Boolean = false,
        var throwErrors: Boolean = false,
        var public: Boolean = false,
        var sourceSet: SourceSet = SourceSet.CommonTest_Via_JVM
    ) {
        @Suppress("ClassName")
        sealed class SourceSet {
            object CommonMain : SourceSet()
            object CommonTest_Via_JVM : SourceSet()
        }
    }

    private val Project.kotlinExtension get() = project.extensions.findByType<KotlinMultiplatformExtension>() ?: throw GradleException("Could not find Kotlin/Multiplatform plugin")

    private fun addKSPDependency(project: Project, kspConfiguration: String) {
        project.dependencies {
            kspConfiguration("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
        }
    }

    private fun addRuntimeDependencies(sourceSet: KotlinSourceSet, ext: Extension) {
        sourceSet.dependencies {
            implementation("org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
            if (ext.usesHelper) {
                implementation("org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}")
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
            "jvm" -> addKSPDependency(project, "ksp${jvmTarget.name.capitalize()}Test")
            "android" -> addKSPDependency(project, "ksp${jvmTarget.name.capitalize()}TestDebug")
        }

        project.afterEvaluate {
            val commonTest = kotlin.sourceSets.getByName("commonTest")

            addRuntimeDependencies(commonTest, ext)

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
                        "jvm" -> dependsOn("kspTestKotlin${jvmTarget.name.capitalize()}")
                        "android" -> dependsOn("kspDebugUnitTestKotlin${jvmTarget.name.capitalize()}")
                    }
                }
            }
        }
    }

    private fun executeOnMain(project: Project, ext: Extension) {
        addKSPDependency(project, "kspCommonMainMetadata")
        addRuntimeDependencies(project.kotlinExtension.sourceSets["commonMain"], ext)
        configureKsp(project, ext)
    }

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension()
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            when (ext.sourceSet) {
                Extension.SourceSet.CommonMain -> executeOnMain(target, ext)
                Extension.SourceSet.CommonTest_Via_JVM -> executeOnTests(target, ext)
            }

        }
    }

}
