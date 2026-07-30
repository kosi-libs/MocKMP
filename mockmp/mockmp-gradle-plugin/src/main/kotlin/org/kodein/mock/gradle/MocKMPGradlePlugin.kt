package org.kodein.mock.gradle

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.HasDeviceTests
import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.HasUnitTest
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.util.*


public class MocKMPGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.add("mockmp", Extension(target))
    }

    public enum class Helper { AutoDetect, JUnit4, JUnit5 }

    public class Options {
        internal var helper: Helper? = null
        internal var throwErrors: Boolean = false
        internal var public: Boolean = false
        internal var accessorsPackage: String = "org.kodein.mock.generated"
        internal var specificTargets: Set<String>? = null

        public val junit4: Helper = Helper.JUnit4
        public val junit5: Helper = Helper.JUnit5

        @JvmOverloads
        public fun withHelper(helper: Helper = Helper.AutoDetect) { this.helper = helper }

        @JvmOverloads
        public fun throwErrors(throwErrors: Boolean = true) { this.throwErrors = throwErrors }

        @JvmOverloads
        public fun public(public: Boolean = true) { this.public = public }

        public fun accessorsPackage(accessorsPackage: String) { this.accessorsPackage = accessorsPackage }

        public fun targets(vararg targets: String) { specificTargets = targets.toSet() }
        public fun allTargets() { specificTargets = null }
    }

    public class Extension(private val project: Project) {
        @JvmOverloads
        public fun onTest(confOptions: Action<Options>? = null) {
            val options = Options()
            confOptions?.execute(options)

            val kotlin =
                project.extensions.findByName("kotlin") as? KotlinProjectExtension
                    ?: error("MocKMP could not find the Kotlin plugin")
            val (sourceSetName, implementationConfigurationName) = if (kotlin is KotlinAndroidProjectExtension) {
                val android = project.extensions.findByName("android") as CommonExtension
                "test" to android.sourceSets["test"].implementationConfigurationName
            } else {
                val sourceSet = kotlin.sourceSets[if (kotlin is KotlinMultiplatformExtension) "commonTest" else "test"]
                sourceSet.name to sourceSet.implementationConfigurationName
            }

            addRuntimeDependencies(
                implementationConfigurationName = implementationConfigurationName,
                helper = options.helper,
                canAutodetect = true
            )

            val ksp = project.extensions.findByType<KspExtension>()
                ?: error("MocKMP could not find the KSP plugin")

            configureKspProcessor(
                ksp = ksp,
                options = options,
                multiplatform = kotlin is KotlinMultiplatformExtension
            )

            val extract = registerExtractTask(
                kotlin = kotlin,
                sourceSetName = sourceSetName,
                options = options,
            )
            if (kotlin is KotlinAndroidProjectExtension) {
                val androidComponents = project.extensions.findByName("androidComponents") as? AndroidComponentsExtension<*, *, *>
                    ?: error("MocKMP could not find the Android plugin")
                androidComponents.onVariants {
                    (it as? HasUnitTest)?.unitTest?.sources?.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                    (it as? HasAndroidTest)?.androidTest?.sources?.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                    (it as? HasHostTests)?.hostTests?.forEach { (_, test) -> test.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory) }
                    (it as? HasDeviceTests)?.deviceTests?.forEach { (_, test) -> test.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory) }
                }
            } else {
                val sourceSet = kotlin.sourceSets[if (kotlin is KotlinMultiplatformExtension) "commonTest" else "test"]
                sourceSet.kotlin.srcDir(extract)
            }

            addKspDependencies(
                kotlin = kotlin,
                suffix = "Test",
                options = options,
            )
        }

        @JvmOverloads
        public fun onMain(confOptions: Action<Options>? = null) {
            val options = Options()
            confOptions?.execute(options)

            val kotlin =
                project.extensions.findByName("kotlin") as? KotlinProjectExtension
                    ?: error("MocKMP could not find the Kotlin plugin")

            val (sourceSetName, implementationConfigurationName) = if (kotlin is KotlinAndroidProjectExtension) {
                val android = project.extensions.findByName("android") as CommonExtension
                "main" to android.sourceSets["main"].implementationConfigurationName
            } else {
                val sourceSet = kotlin.sourceSets[if (kotlin is KotlinMultiplatformExtension) "commonMain" else "main"]
                sourceSet.name to sourceSet.implementationConfigurationName
            }

            addRuntimeDependencies(
                implementationConfigurationName = implementationConfigurationName,
                helper = options.helper,
                canAutodetect = false
            )

            val ksp = project.extensions.findByType<KspExtension>()
                ?: error("MocKMP could not find the KSP plugin")

            configureKspProcessor(
                ksp = ksp,
                options = options,
                multiplatform = kotlin is KotlinMultiplatformExtension
            )

            val extract = registerExtractTask(
                kotlin = kotlin,
                sourceSetName = sourceSetName,
                options = options,
            )
            if (kotlin is KotlinAndroidProjectExtension) {
                val androidComponents = project.extensions.findByName("androidComponents") as? AndroidComponentsExtension<*, *, *>
                    ?: error("MocKMP could not find the Android plugin")
                androidComponents.onVariants {
                    it.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                }
            } else {
                val sourceSet = kotlin.sourceSets[if (kotlin is KotlinMultiplatformExtension) "commonMain" else "main"]
                sourceSet.kotlin.srcDir(extract)
            }

            addKspDependencies(
                kotlin = kotlin,
                suffix = "",
                options = options,
            )
        }

        private fun addRuntimeDependencies(
            implementationConfigurationName: String,
            helper: Helper?,
            canAutodetect: Boolean,
        ) {
            project.dependencies.add(implementationConfigurationName, "org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
            if (helper != null) {
                project.dependencies.add(implementationConfigurationName, project.provider {
                    val isJunit5 = when (helper) {
                        Helper.JUnit4 -> false
                        Helper.JUnit5 -> true
                        Helper.AutoDetect -> {
                            if (canAutodetect) {
                                project.tasks.withType<Test>().any { it.testFramework is JUnitPlatformTestFramework }
                            } else {
                                error("MocKMP cannot auto-detect JUnit version, please use withHelper(junit4) or withHelper(junit5)")
                            }
                        }
                    }
                    if (isJunit5) "org.kodein.mock:mockmp-test-helper-junit5:${BuildConfig.VERSION}"
                    else "org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}"
                })
            }
        }

        private fun configureKspProcessor(
            ksp: KspExtension,
            options: Options,
            multiplatform: Boolean
        ) {
            if (options.throwErrors) {
                ksp.arg("org.kodein.mock.errors", "throw")
            }
            if (options.public) {
                ksp.arg("org.kodein.mock.visibility", "public")
            }
            ksp.arg("org.kodein.mock.package", options.accessorsPackage)
            ksp.arg("org.kodein.mock.multiplatform", multiplatform.toString())
        }

        private fun registerExtractTask(
            kotlin: KotlinProjectExtension,
            sourceSetName: String,
            options: Options
        ): TaskProvider<MocKMPExtractExpectKt> =
            project.tasks.register<MocKMPExtractExpectKt>("mockmpExtractExpectKt") {
                outputDirectory.set(project.layout.buildDirectory.get().asFile.resolve("mockmp/$sourceSetName/kotlin"))
                accessorsPackage.set(options.accessorsPackage)
                public.set(options.public)
                resource.set("/mockmp.${if (kotlin is KotlinMultiplatformExtension) "multi" else "single"}.kt")
            }

        private fun addKspDependencies(kotlin: KotlinProjectExtension, suffix: String, options: Options) {
            val kotlinTargets = when (kotlin) {
                is KotlinMultiplatformExtension -> {
                    when (val targets = options.specificTargets) {
                        null -> kotlin.targets.filterNot { it.name == "metadata" }
                        else -> kotlin.targets.filter { it.name in targets }
                    }
                }
                is KotlinSingleTargetExtension<*> -> {
                    require(options.specificTargets == null) { "Cannot specify MocKMP targets in a single target kotlin project." }
                    listOf(kotlin.target)
                }
                else -> error("Unexpected 'kotlin' extension $kotlin")
            }

            kotlinTargets.forEach { target ->
                val configurations = if (suffix.isNotEmpty()) {
                    project.configurations
                        .filter { it.name.startsWith("ksp${target.name.capitalized()}") && it.name.endsWith(suffix) }
                        .map { it.name }
                } else {
                    listOf("ksp${target.name.capitalized()}")
                }

                configurations.forEach { configuration ->
                    if (configuration in project.configurations.names) {
                        project.dependencies.add(
                            configuration,
                            "org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}",
                        )
                    } else {
                        project.logger.error("Configuration '$configuration' not found for target '${target.name}'.")
                    }
                }
            }
        }
    }
}

private fun String.capitalized(locale: Locale = Locale.getDefault()) = replaceFirstChar {
    when {
        it.isLowerCase() -> it.titlecase(locale)
        else -> it.toString()
    }
}
