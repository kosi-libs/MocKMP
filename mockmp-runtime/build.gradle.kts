plugins {
    id("org.kodein.library.mpp")
}

val kspVersion: String by extra

kodein {
    kotlin {
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation("org.objenesis:objenesis:3.2")
                implementation("org.javassist:javassist:3.28.0-GA")
            }
        }
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)
    }
}

kodeinUpload {
    name = "mockmp-runtime"
    description = "MocKMP runtime"
}

// TODO: remove this when moving to Kotlin 1.6.20
// Support M1 (patch for now due to KotlinJS relying on a version 14 not available for arm64)
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
