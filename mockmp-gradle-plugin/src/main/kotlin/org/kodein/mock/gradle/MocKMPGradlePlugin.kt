package org.kodein.mock.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask


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
        private val project: Project,
        var usesHelper: Boolean = false,
        var throwErrors: Boolean = false,
        var public: Boolean = false,
        var targetSourceSet: SourceSetTarget = SourceSetTarget.CommonTest_Via_JVM
    ) {
        val CommonMain get() = SourceSetTarget.CommonMain
        val CommonTest_Via_JVM get() = SourceSetTarget.CommonTest_Via_JVM

        internal var workaroundInstalled = false

        fun installWorkaround() {
            execute(project, this)
            workaroundInstalled = true
        }
    }

    private companion object {
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
            sourceSet.kotlin.srcDir(
                "build/generated/ksp/${
                    sourceSet.name.removeSuffix("Test").removeSuffix("Main")
                }/${sourceSet.name}Test/kotlin"
            )
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

            val jvmTarget = kotlin.targets.firstOrNull { it.platformType == KotlinPlatformType.jvm }
                ?: kotlin.targets.firstOrNull { it.platformType == KotlinPlatformType.androidJvm }
                ?: throw GradleException("Could not find JVM or Android target")

            when (jvmTarget.platformType) {
                KotlinPlatformType.jvm -> addKSPDependency(project, "ksp${jvmTarget.name.replaceFirstChar { it.titlecase() }}Test")
                KotlinPlatformType.androidJvm -> addKSPDependency(project, "ksp${jvmTarget.name.replaceFirstChar { it.titlecase() }}TestDebug")
                else -> error("Unsupported platform type ${jvmTarget.platformType}")
            }

            project.afterEvaluate {
                val commonTest = kotlin.sourceSets.getByName("commonTest")

                addRuntimeDependencies(project, commonTest, ext)

                configureKsp(project, ext)

                project.tasks.withType<KotlinCompilationTask<*>>().all {
                    if (name.startsWith("compile") && name.contains("TestKotlin")) {
                        when (jvmTarget.platformType) {
                            KotlinPlatformType.jvm -> dependsOn("kspTestKotlin${jvmTarget.name.replaceFirstChar { it.titlecase() }}")
                            KotlinPlatformType.androidJvm -> dependsOn("kspDebugUnitTestKotlin${jvmTarget.name.replaceFirstChar { it.titlecase() }}")
                            else -> error("Unsupported platform type ${jvmTarget.platformType}")
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

        private fun execute(project: Project, ext: Extension) {
            when (ext.targetSourceSet) {
                SourceSetTarget.CommonMain -> executeOnMain(project, ext)
                SourceSetTarget.CommonTest_Via_JVM -> executeOnTests(project, ext)
            }
        }
    }

    private class MocKMPGradlePluginInstallMissing
        : Exception(
        """
            Please add the following code AFTER your kotlin AND your optional mockmp configuration block:
                mockmp.installWorkaround()
            
            This is now required because of this KSP issue:
                https://github.com/google/ksp/issues/1524
            
            You can have a look at the plugin documentation for its usage information:
                https://github.com/kosi-libs/MocKMP#with-the-official-plugin
        """.trimIndent()) {
    }

    override fun apply(target: Project) {
        target.plugins.apply("com.google.devtools.ksp")

        val ext = Extension(target)
        target.extensions.add("mockmp", ext)

        target.afterEvaluate {
            if (!ext.workaroundInstalled) throw MocKMPGradlePluginInstallMissing()
        }
    }

}
