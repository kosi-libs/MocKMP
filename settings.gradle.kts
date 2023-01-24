buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://maven.pkg.github.com/kosi-libs/kodein-internal-gradle-plugin") {
            credentials {
                username = extra["github.username"]?.toString()
                    ?: error("Please set github.username in ~/.gradle/gradle.properties")
                password = extra["github.personalAccessToken"]?.toString()
                    ?: error("Please set github.personalAccessToken in ~/.gradle/gradle.properties")
            }
        }
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
