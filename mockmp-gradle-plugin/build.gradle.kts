plugins {
    id("org.kodein.gradle-plugin")
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

apply(from = "../maven-publishing-setup.kts")

val kspVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlin.coreLibrariesVersion}")
}

gradlePlugin.plugins.create("mockmp") {
    id = "org.kodein.mock.mockmp"
    implementationClass = "org.kodein.mock.gradle.MocKMPGradlePlugin"
    displayName = "MocKMP"
    description = "Applies the MocKMP symbol processor to a Kotlin/Multiplatform project"
}

pluginBundle {
    description = "Applies the MocKMP symbol processor to a Kotlin/Multiplatform project"
    tags = listOf("kotlin", "mock", "test")
}

buildConfig {
    packageName("org.kodein.mock.gradle")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xsuppress-version-warnings"
    }
}

kodeinUpload {
    name = "mockmp-gradle-plugin"
    description = "MocKMP Gradle Plugin"
}
