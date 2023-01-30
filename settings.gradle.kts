buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:7.0.0")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "MocKMP"

include(
    ":mockmp-runtime",
    ":mockmp-processor",
    ":test-helper:mockmp-test-helper",
    ":test-helper:mockmp-test-helper-junit5",
    ":mockmp-gradle-plugin",
    ":tests:tests-junit4",
    ":tests:tests-junit5",
)
