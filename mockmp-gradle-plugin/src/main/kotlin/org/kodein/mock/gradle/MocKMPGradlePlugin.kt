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


// The entire purpose of this Gradle plugin is to get around https://github.com/google/ksp/issues/567
// This issue is scheduled to be fixed in KSP 1.1 (according to milestone defined in the issue).
// Common Google, having KSP work in multiplatform correctly must be very important!
class MocKMPGradlePlugin : Plugin<Project> {

    class Extension(var usesHelper: Boolean = false, val throwErrors: Boolean = false)

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension()
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: throw GradleException("Could not find Kotlin/Multiplatform plugin")

            val jvmTarget = kotlin.targets.first { it.preset?.name == "jvm" || it.preset?.name == "android" }

            dependencies {
                // Running KSP for JVM only
                "ksp${jvmTarget.name.capitalize()}Test"("org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}")
            }

            afterEvaluate {
                val testComps = jvmTarget.compilations.mapNotNull {
                    val compilation = it.compileKotlinTask
                    val ksp = tasks.findByName("ksp${it.name.capitalize()}Kotlin${jvmTarget.name.capitalize()}") as? KspTask
                    if ((it.name == "test" || it.name.endsWith("Test")) && ksp != null) {
                        Triple(it.name, compilation, ksp)
                    } else null
                }.takeIf { it.isNotEmpty() } ?: error("Could not find test configuration with associated KSP on target ${jvmTarget.name}")

                // Adding KSP JVM result to COMMON source set
                kotlin.sourceSets.getByName("commonTest") {
                    this.kotlin.srcDirs(
                        testComps.map { (name, _, _) -> "$buildDir/generated/ksp/${jvmTarget.name}/${jvmTarget.name}${name.capitalize()}/kotlin" }
                    )
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

                // Adding KSP JVM as a dependency to all Kotlin compilations
                tasks.withType<KotlinCompile<*>>().all {
                    if (name.startsWith("compileTestKotlin")) {
                        val (name, _, _) = testComps.first()
                        dependsOn("ksp${name.capitalize()}Kotlin${jvmTarget.name.capitalize()}")
                    }
                }

                // Each KSP test generation needs to delete the result of other KSP test generations (because they are all considered source set, they would collide).
                // Furthermore, to prevent race condition in case multiple test compilation are run, each KSP generation needs to happen *after* the previous compilations.
                testComps.forEachIndexed { index, (_, _, ksp) ->
                    ksp.doFirst {
                        testComps.filterIndexed { i, _ -> i != index } .forEach { (name, _, _) ->
                            delete("$buildDir/generated/ksp/${jvmTarget.name}/${jvmTarget.name}${name.capitalize()}/kotlin")
                        }
                    }
                    testComps.subList(0, index).forEach { (_, compile, _) ->
                        ksp.mustRunAfter(compile)
                    }
                }
            }
        }
    }

}
