plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlin.plugin.samWithReceiver)
    alias(libs.plugins.buildConfig)
}

dependencies {
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.android.gradlePlugin)

    // ProjectBuilder comes from gradleApi(), which `kotlin-dsl` puts on the main classpath but not the
    // test one. No TestKit: these tests configure a project in memory rather than running a build.
    testImplementation(gradleApi())
    testImplementation(libs.kotlin.test.junit)
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

// The plugin goes to the Gradle Plugin Portal (publishPlugins) *and*, like every other module, to
// Maven Central (publishAndReleaseToMavenCentral): the portal is not a repository a build can resolve
// an ordinary `classpath`/`implementation` dependency from, which is what a legacy `buildscript {}`
// block, or a project that mirrors its dependencies, needs.
// `java-gradle-plugin` publishes two coordinates — the plugin itself and the `org.kodein.mock.mockmp`
// marker that maps the plugin id onto it — and both carry this POM.
mavenPublishing {
    pom {
        name = "mockmp-gradle-plugin"
        description = "MocKMP Gradle plugin"
    }
}

buildConfig {
    packageName("org.kodein.mock.gradle")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

kotlin {
    explicitApi()
}
