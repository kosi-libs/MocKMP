pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }

    resolutionStrategy.eachPlugin {
        if (target.id.id == "org.kodein.mock.mockmp") {
            val rx = Regex("version = \"(.+)\"")
            val match = file("$rootDir/../../build.gradle.kts").useLines { lines ->
                lines.mapNotNull { rx.matchEntire(it.trim()) }.firstOrNull()
            } ?: error("Could not find parent project version")
            val mockmpVersion = match.groupValues[1]

            useModule("org.kodein.mock:mockmp-gradle-plugin:$mockmpVersion")
        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
    }

    versionCatalogs {
        create("kodeinGlobals") {
            val rx = Regex("classpath\\(\"org.kodein.internal.gradle:kodein-internal-gradle-settings:(.+)\"\\)")
            val match = file("$rootDir/../../settings.gradle.kts").useLines { lines ->
                lines.mapNotNull { rx.matchEntire(it.trim()) }.firstOrNull()
            } ?: error("Could not find parent KIGP version")
            val kigpVersion = match.groupValues[1]

            from("org.kodein.internal.gradle:kodein-internal-gradle-version-catalog:$kigpVersion")
        }
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
