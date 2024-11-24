pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
    }

    versionCatalogs {
        create("kodeinGlobals") {
            val rx = Regex("classpath\\(\"org.kodein.internal.gradle:kodein-internal-gradle-settings:(.+)\"\\)")
            val match = file("$rootDir/../mockmp/settings.gradle.kts").useLines { lines ->
                lines.mapNotNull { rx.matchEntire(it.trim()) }.firstOrNull()
            } ?: error("Could not find parent KIGP version")
            val kigpVersion = match.groupValues[1]

            from("org.kodein.internal.gradle:kodein-internal-gradle-version-catalog:$kigpVersion")
        }
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
)
