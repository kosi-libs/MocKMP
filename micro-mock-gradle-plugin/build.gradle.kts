plugins {
    kotlin("jvm")
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
}

val kspVersion: String by rootProject.extra
val kotlinVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

gradlePlugin.plugins.create("microMock") {
    id = "org.kodein.micromock"
    implementationClass = "org.kodein.micromock.gradle.MicroMockGradlePlugin"
}
