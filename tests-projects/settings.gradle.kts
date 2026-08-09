rootProject.name = "tests-projects"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("../mockmp/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../mockmp")

include(
    ":tests-mp-junit4",
    ":tests-mp-junit5",
    ":tests-mp-android",
    ":tests-jvm-junit4",
    ":tests-jvm-junit5",
    ":tests-android",
    ":tests-mp-main",
    ":tests-mp-empty",
    ":tests-jvm-empty",
    ":tests-mp-options",
)
