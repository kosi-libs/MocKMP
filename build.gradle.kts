plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.6.10-1.0.2" apply false
}

val kspVersion by extra { "1.6.10-1.0.2" }

allprojects {
    group = "org.kodein.mock"
    version = "1.2.0"
}

// Support M1 (patch for now due to KotlinJS relying on a version 14 not available for arm64)
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
