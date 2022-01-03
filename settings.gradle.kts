buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://raw.githubusercontent.com/Kodein-Framework/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:6.16.0")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "MocKMP"

include(
    ":mockmp-runtime",
    ":mockmp-processor",
    ":mockmp-test-helper",
    ":mockmp-gradle-plugin",
    ":tests",
)
