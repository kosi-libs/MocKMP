plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.kotlin.plugin.samWithReceiver)
    alias(libs.plugins.buildConfig)
}

dependencies {
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.android.gradlePlugin)
}

gradlePlugin {
    // The "Website" and "Source repository" links of the Gradle Plugin Portal listing.
    website.set("https://kosi-libs.org/mockmp/")
    vcsUrl.set("https://github.com/kosi-libs/MocKMP")

    plugins.register("mockmp") {
        id = "org.kodein.mock.mockmp"
        implementationClass = "org.kodein.mock.gradle.MocKMPGradlePlugin"
        displayName = "MocKMP"
        description = "Applies the MocKMP symbol processor to a Kotlin Multiplatform, Android or JVM project"
        tags.set(listOf("kotlin", "mock", "test"))
    }
}

buildConfig {
    packageName("org.kodein.mock.gradle")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

kotlin {
    explicitApi()
}
