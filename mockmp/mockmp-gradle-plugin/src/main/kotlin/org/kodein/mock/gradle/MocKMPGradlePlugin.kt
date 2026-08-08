package org.kodein.mock.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.HasDeviceTests
import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.HasUnitTest
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
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

            fun AndroidComponentsExtension<*,*,*>.installExtractor() {
                onVariants {
                    (it as? HasUnitTest)?.unitTest?.sources?.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                    (it as? HasAndroidTest)?.androidTest?.sources?.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                    (it as? HasHostTests)?.hostTests?.forEach { (_, test) -> test.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory) }
                    (it as? HasDeviceTests)?.deviceTests?.forEach { (_, test) -> test.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory) }
                }
            }

            if (kotlin is KotlinAndroidProjectExtension) {
                val androidComponents = project.extensions.findByName("androidComponents") as? AndroidComponentsExtension<*, *, *>
                    ?: error("MocKMP could not find the Android plugin")
                androidComponents.installExtractor()
            } else {
                val sourceSet = kotlin.sourceSets[if (kotlin is KotlinMultiplatformExtension) "commonTest" else "test"]
                sourceSet.kotlin.srcDir(extract)
                val androidComponents = project.extensions.findByName("androidComponents") as? AndroidComponentsExtension<*, *, *>
                androidComponents?.installExtractor()
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
                val androidComponents = project.extensions.findByName("androidComponents") as? AndroidComponentsExtension<*, *, *>
                androidComponents?.onVariants {
                    it.sources.kotlin?.addGeneratedSourceDirectory(extract, MocKMPExtractExpectKt::outputDirectory)
                }
            }

            addKspDependencies(
                kotlin = kotlin,
                suffix = "",
                options = options,
            )
        }

        private fun addHelperDependency(implementationConfigurationName: String, isJunit5: Boolean) {
            project.dependencies.add(
                implementationConfigurationName,
                if (isJunit5) "org.kodein.mock:mockmp-test-helper-junit5:${BuildConfig.VERSION}"
                else "org.kodein.mock:mockmp-test-helper:${BuildConfig.VERSION}"
            )
        }

        /**
         * True when any test task runs on the JUnit Platform.
         *
         * `Test.getOptions()` returns the options of whichever framework the task is set to, so the
         * type of that object is the public counterpart of the internal `getTestFramework()`.
         * Realizing each task here is what runs its pending configuration actions, which is how a
         * `useJUnitPlatform()` buried in a lazy `configure { }` — the Kotlin Multiplatform DSL's
         * `testRuns["test"].executionTask` being the common case — is seen at all.
         */
        private fun usesJUnitPlatform(): Boolean =
            project.tasks.withType<Test>().any { it.options is JUnitPlatformOptions }

        private fun addRuntimeDependencies(
            implementationConfigurationName: String,
            helper: Helper?,
            canAutodetect: Boolean,
        ) {
            project.dependencies.add(implementationConfigurationName, "org.kodein.mock:mockmp-runtime:${BuildConfig.VERSION}")
            when (helper) {
                null -> {}
                Helper.JUnit4 -> addHelperDependency(implementationConfigurationName, isJunit5 = false)
                Helper.JUnit5 -> addHelperDependency(implementationConfigurationName, isJunit5 = true)
                Helper.AutoDetect -> {
                    if (!canAutodetect) error("MocKMP cannot auto-detect JUnit version, please use withHelper(junit4) or withHelper(junit5)")
                    // In afterEvaluate rather than in a dependency provider: a provider is queried
                    // while resolving the configuration, which happens at execution time, where
                    // reaching back into the project is what the configuration cache forbids. By
                    // afterEvaluate the build script has run, and any plugin that creates test tasks
                    // from its own afterEvaluate registered that callback when it was applied — above
                    // the mockmp { } block, so ahead of this one.
                    project.afterEvaluate {
                        addHelperDependency(implementationConfigurationName, isJunit5 = usesJUnitPlatform())
                    }
                }
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
                // A provider, not buildDirectory.get(): resolving it here would pin an absolute path
                // at configuration time, leaving this task writing to the old location if anything
                // relocates the build directory afterwards.
                outputDirectory.set(project.layout.buildDirectory.dir("mockmp/$sourceSetName/kotlin"))
                accessorsPackage.set(options.accessorsPackage)
                public.set(options.public)
                resource.set("/mockmp.${if (kotlin is KotlinMultiplatformExtension) "multi" else "single"}.kt")
            }

        private fun addKspDependencies(kotlin: KotlinProjectExtension, suffix: String, options: Options) {
            val kotlinTargets = when (kotlin) {
                is KotlinMultiplatformExtension -> {
                    when (val targets = options.specificTargets) {
                        null -> kotlin.targets.filterNot { it.name == "metadata" }
                        else -> {
                            // Otherwise a mistyped name simply filters everything out, and MocKMP
                            // quietly processes nothing at all.
                            val unknown = targets - kotlin.targets.map { it.name }.toSet()
                            if (unknown.isNotEmpty()) {
                                error(
                                    "MocKMP was configured with unknown target(s) ${unknown.joinToString()}. " +
                                            "This project declares ${kotlin.targets.joinToString { it.name }}."
                                )
                            }
                            kotlin.targets.filter { it.name in targets }
                        }
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

                // Failing rather than logging, as everything else in this plugin does: a target left
                // unprocessed does not fail here, it fails much later and elsewhere, as the accessors
                // it should have generated turning up missing — "expect … has no actual declaration".
                if (configurations.isEmpty()) {
                    error(
                        "MocKMP found no KSP configuration for target '${target.name}'. " +
                                "Make sure the KSP plugin is applied and supports that target."
                    )
                }

                configurations.forEach { configuration ->
                    if (configuration !in project.configurations.names) {
                        error(
                            "MocKMP could not find the KSP configuration '$configuration' for target '${target.name}'. " +
                                    "Make sure the KSP plugin is applied and supports that target."
                        )
                    }
                    project.dependencies.add(
                        configuration,
                        "org.kodein.mock:mockmp-processor:${BuildConfig.VERSION}",
                    )
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
