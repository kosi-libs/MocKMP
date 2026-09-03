package org.kodein.mock.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Writes one small file from a bundled resource with two string substitutions — cheaper to re-run than to fetch from the cache")
internal abstract class MocKMPExtractExpectKt : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val accessorsPackage: Property<String>

    @get:Input
    abstract val public: Property<Boolean>

    @get:Input
    abstract val resource: Property<String>

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
