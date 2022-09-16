pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
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
