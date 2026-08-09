package org.kodein.mock.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

/**
 * The plugin options whose whole behaviour is to *reject* a configuration. They cannot be covered by a
 * test project the way `public()`, `accessorsPackage()`, `targets()` and `withHelper(junit5)` are in
 * `tests-mp-options` — there, compiling is the assertion; here, the point is that configuration never
 * gets that far.
 *
 * ProjectBuilder rather than TestKit: these paths all fail during `mockmp { }` evaluation, so an
 * in-memory project is enough and no nested build has to run.
 */
class PluginOptionsTests {

    // KSP as well as Kotlin: onTest/onMain look the ksp extension up before most of what is checked
    // here, so without it every test would fail on "could not find the KSP plugin" instead.
    private fun project(kotlinPluginId: String) =
        ProjectBuilder.builder().build().also {
            it.plugins.apply(kotlinPluginId)
            it.plugins.apply("com.google.devtools.ksp")
            it.plugins.apply(MocKMPGradlePlugin::class.java)
        }

    private fun mockmp(kotlinPluginId: String) =
        project(kotlinPluginId).extensions.getByName("mockmp") as MocKMPGradlePlugin.Extension

    // onMain cannot detect the JUnit version: there are no test tasks to look at, so withHelper() with
    // no argument has nothing to go on and must say so rather than guess.
    @Test
    fun onMainCannotAutodetectTheJUnitVersion() {
        val ex = assertFailsWith<IllegalStateException> {
            mockmp("org.jetbrains.kotlin.jvm").onMain { withHelper() }
        }
        assertContains(ex.message!!, "cannot auto-detect JUnit version")
    }

    // ...whereas naming the version explicitly is exactly what the message asks for, and works.
    @Test
    fun onMainAcceptsAnExplicitJUnitVersion() {
        mockmp("org.jetbrains.kotlin.jvm").onMain { withHelper(junit5) }
    }

    // A single-target project has one target by construction, so naming targets is meaningless rather
    // than merely redundant.
    @Test
    fun targetsIsRejectedOnASingleTargetProject() {
        val ex = assertFailsWith<IllegalArgumentException> {
            mockmp("org.jetbrains.kotlin.jvm").onTest { targets("jvm") }
        }
        assertContains(ex.message!!, "Cannot specify MocKMP targets in a single target kotlin project.")
    }

    // A mistyped target name used to filter everything out silently, leaving MocKMP processing nothing
    // and the failure surfacing much later as missing accessors. `jvm` is declared here so the message
    // has to single out the typo rather than reject both, which is what makes this about the lookup
    // and not merely about an empty target list.
    @Test
    fun anUnknownTargetNameIsRejected() {
        val project = project("org.jetbrains.kotlin.multiplatform")
        (project.extensions.getByName("kotlin") as KotlinMultiplatformExtension).jvm()

        val ex = assertFailsWith<IllegalStateException> {
            (project.extensions.getByName("mockmp") as MocKMPGradlePlugin.Extension)
                .onTest { targets("jvm", "typo") }
        }
        assertContains(ex.message!!, "unknown target(s) typo")
    }
}
