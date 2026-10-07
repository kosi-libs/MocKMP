package org.kodein.mock.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Gradle task that extracts the MocKMP expect/helper declaration template into the project's
 * generated source directory.
 *
 * Depending on whether the project is multiplatform or single-target, this task reads the bundled
 * `mockmp.multi.kt` or `mockmp.single.kt` resource and writes `mockmp.expect.kt` after substituting
 * template variables:
 * - `{PACKAGE}`: package name for the generated accessors (from [accessorsPackage])
 * - `{VISIBILITY}`: declaration visibility, `"public"` or `"internal"` (from [public])
 */
@DisableCachingByDefault(because = "Writes one small file from a bundled resource with two string substitutions — cheaper to re-run than to fetch from the cache")
internal abstract class MocKMPExtractExpectKt : DefaultTask() {

    /**
     * Target directory where the generated `mockmp.expect.kt` file is written.
     *
     * Configured as a source directory for the corresponding Kotlin / Android source set.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * The package name under which the expect/helper functions are generated.
     */
    @get:Input
    abstract val accessorsPackage: Property<String>

    /**
     * Whether the generated expect/helper functions should have `public` or `internal` visibility.
     */
    @get:Input
    abstract val public: Property<Boolean>

    /**
     * The classpath resource path of the template file to extract (`/mockmp.multi.kt` or `/mockmp.single.kt`).
     */
    @get:Input
    abstract val resource: Property<String>

    /**
     * Reads the template resource, applies string substitutions, and writes the resulting `mockmp.expect.kt`.
     */
    @TaskAction
    internal fun execute() {
        val outputFile = outputDirectory.get().asFile.resolve("mockmp.expect.kt")
        outputFile.parentFile.mkdirs()
        outputFile.outputStream().writer().use { output ->
            val text = MocKMPGradlePlugin::class.java.getResourceAsStream(resource.get())!!.use { input ->
                input.reader().readText()
            }
            output.append(
                text
                    .replace("{PACKAGE}", accessorsPackage.get())
                    .replace("{VISIBILITY}", if (public.get()) "public" else "internal")
            )
        }
    }
}
