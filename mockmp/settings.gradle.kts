buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:8.9.0")
        classpath("org.gradle.toolchains:foojay-resolver:0.8.0")
    }
}

apply {
    plugin("org.kodein.settings")
    plugin("org.gradle.toolchains.foojay-resolver-convention")
}

include(
    ":mockmp-runtime",
    ":mockmp-processor",
    ":test-helper:mockmp-test-helper",
    ":test-helper:mockmp-test-helper-junit5",
    ":mockmp-gradle-plugin",
)
