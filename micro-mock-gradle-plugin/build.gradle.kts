plugins {
    kotlin("jvm")
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

val kspVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlin.coreLibrariesVersion}")
}

gradlePlugin.plugins.create("microMock") {
    id = "org.kodein.micromock"
    implementationClass = "org.kodein.micromock.gradle.MicroMockGradlePlugin"
}

buildConfig {
    packageName("org.kodein.micromock.gradle")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}
