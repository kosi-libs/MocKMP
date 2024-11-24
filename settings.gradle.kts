rootProject.name = "Kosi-MocKMP"

// https://youtrack.jetbrains.com/issue/IDEA-343588
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

includeBuild("mockmp")
includeBuild("tests-projects")
