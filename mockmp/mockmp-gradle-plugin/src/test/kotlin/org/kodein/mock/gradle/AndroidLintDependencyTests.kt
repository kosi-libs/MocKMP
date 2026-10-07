package org.kodein.mock.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Under `com.android.kotlin.multiplatform.library`, AGP lint reads the Kotlin source directories of the
 * source sets as plain files, so the producers of those directories — MocKMP's extract task and the KSP
 * task of the linted component — were not dependencies of the lint tasks, and Gradle failed the build
 * as soon as both ran in the same invocation (`check` + `kspAndroidHostTest`).
 */
class AndroidLintDependencyTests {

    private fun kmpAndroidProject(): Project =
        ProjectBuilder.builder().build().also { project ->
            project.plugins.apply("org.jetbrains.kotlin.multiplatform")
            project.plugins.apply("com.android.kotlin.multiplatform.library")
            project.plugins.apply("com.google.devtools.ksp")
            project.plugins.apply(MocKMPGradlePlugin::class.java)

            val kotlin = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
            (kotlin as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("android") {
                namespace = "org.kodein.mock.test"
                compileSdk = 37
                minSdk = 24
                withHostTest {}
            }
            kotlin.jvm()

            // Another tool's lint task, which has nothing to do with Android or MocKMP.
            project.tasks.register("lintKotlin")

            (project.extensions.getByName("mockmp") as MocKMPGradlePlugin.Extension).onTest()
            (project as ProjectInternal).evaluate()
        }

    // The explicit dependencies only: resolving the whole graph of an AGP lint task looks the Android
    // SDK up, which a unit test cannot count on. The explicit ones are exactly what MocKMP adds.
    private fun Project.dependencyNames(taskName: String): Set<String> =
        tasks.getByName(taskName).dependsOn.flatMap { dep ->
            when (dep) {
                is TaskProvider<*> -> listOf(dep.name)
                is Task -> listOf(dep.name)
                is Iterable<*> -> dep.filterIsInstance<Task>().map { it.name }
                else -> emptyList()
            }
        }.toSet()

    @Test
    fun androidLintAnalysisDependsOnTheExtractAndKspTasks() {
        val deps = kmpAndroidProject().dependencyNames("lintAnalyzeAndroidHostTest")
        assertTrue("mockmpExtractExpectKt" in deps, "Dependencies: $deps")
        assertTrue("kspAndroidHostTest" in deps, "Dependencies: $deps")
    }

    @Test
    fun androidLintModelDependsOnTheExtractAndKspTasks() {
        val deps = kmpAndroidProject().dependencyNames("generateAndroidHostTestLintModel")
        assertTrue("mockmpExtractExpectKt" in deps, "Dependencies: $deps")
        assertTrue("kspAndroidHostTest" in deps, "Dependencies: $deps")
    }

    @Test
    fun otherLintTasksAreNotImpacted() {
        val deps = kmpAndroidProject().dependencyNames("lintKotlin")
        assertFalse("mockmpExtractExpectKt" in deps, "Dependencies: $deps")
        assertFalse(deps.any { it.startsWith("ksp") }, "Dependencies: $deps")
    }

    @Test
    fun androidLintTaskNamesAreDerivedFromTheComponent() {
        assertEquals(
            setOf(
                "lintAnalyzeAndroidHostTest",
                "lintVitalAnalyzeAndroidHostTest",
                "generateAndroidHostTestLintModel",
                "generateAndroidHostTestLintReportModel",
                "generateAndroidHostTestLintVitalReportModel",
            ),
            androidLintTaskNames("AndroidHostTest"),
        )
    }
}
