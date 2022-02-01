package org.kodein.mock.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MocKMPGradlePlugin : Plugin<Project> {

    class Extension(var usesHelper: Boolean = false, val throwErrors: Boolean = false)

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension()
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: throw GradleException("Could not find Kotlin/Multiplatform plugin")

            val jvmName = kotlin.targets.first { it.preset?.name == "jvm" || it.preset?.name == "android" } .name

            // Adding KSP JVM result to COMMON source set
            kotlin.sourceSets.getByName("commonTest") {
                this.kotlin.srcDir("build/generated/ksp/${jvmName}Test/kotlin")
                dependencies {
                    implementation("org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
                    if (ext.usesHelper) {
                        implementation("org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}")
                    }
                    if (ext.throwErrors) {
                        target.extensions.getByName<com.google.devtools.ksp.gradle.KspExtension>("ksp").arg("org.kodein.mock.errors", "throw")
                    }
                }
            }

            dependencies {
                // Running KSP for JVM only
                "ksp${jvmName.capitalize()}Test"("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
            }

            // Adding KSP JVM as a dependency to all Kotlin compilations
            tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
                if (name.startsWith("compileTestKotlin")) {
                    dependsOn("kspTestKotlin${jvmName.capitalize()}")
                }
            }
        }
    }

}
