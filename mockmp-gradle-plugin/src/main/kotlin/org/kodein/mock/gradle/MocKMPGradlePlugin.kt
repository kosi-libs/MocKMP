package org.kodein.mock.gradle

import com.google.devtools.ksp.gradle.KspTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation


// The entire purpose of this Gradle plugin is to get around https://github.com/google/ksp/issues/567
// This issue is scheduled to be fixed in KSP 1.1 (according to milestone defined in the issue).
// Common Google, having KSP work in multiplatform correctly must be very important!
class MocKMPGradlePlugin : Plugin<Project> {

    class Extension(
        var usesHelper: Boolean = false,
        var throwErrors: Boolean = false,
        var public: Boolean = false
    )

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension()
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: throw GradleException("Could not find Kotlin/Multiplatform plugin")

            val jvmTarget = kotlin.targets.firstOrNull { it.preset?.name == "jvm" }
                ?: kotlin.targets.firstOrNull { it.preset?.name == "android" }
                ?: throw GradleException("Could not find JVM or Android target")

            dependencies {
                // Running KSP for JVM only
                when (jvmTarget.preset!!.name) {
                    "jvm" -> "ksp${jvmTarget.name.capitalize()}Test"("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
                    "android" -> "ksp${jvmTarget.name.capitalize()}TestDebug"("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
                }
            }

            afterEvaluate {
                // Adding KSP JVM result to COMMON source set
                kotlin.sourceSets.getByName("commonTest") {
                    when (jvmTarget.preset!!.name) {
                        "jvm" -> this.kotlin.srcDir("$buildDir/generated/ksp/${jvmTarget.name}/${jvmTarget.name}Test/kotlin")
                        "android" -> {
                            println("$buildDir/generated/ksp/${jvmTarget.name}/${jvmTarget.name}DebugUnitTest/kotlin")
                            this.kotlin.srcDir("$buildDir/generated/ksp/${jvmTarget.name}/${jvmTarget.name}DebugUnitTest/kotlin")
                        }
                    }

                    dependencies {
                        implementation("org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
                        if (ext.usesHelper) {
                            implementation("org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}")
                        }
                        val ksp = target.extensions.getByName<com.google.devtools.ksp.gradle.KspExtension>("ksp")
                        if (ext.throwErrors) {
                            ksp.arg("org.kodein.mock.errors", "throw")
                        }
                        if (ext.public) {
                            ksp.arg("org.kodein.mock.visibility", "public")
                        }
                    }
                }

                // Adding KSP JVM as a dependency to all Kotlin compilations
                tasks.withType<KotlinCompile<*>>().all {
                    if (name.startsWith("compile") && name.contains("TestKotlin")) {
                        when (jvmTarget.preset!!.name) {
                            "jvm" -> dependsOn("kspTestKotlin${jvmTarget.name.capitalize()}")
                            "android" -> dependsOn("kspDebugUnitTestKotlin${jvmTarget.name.capitalize()}")
                        }
                    }
                }
            }
        }
    }

}
