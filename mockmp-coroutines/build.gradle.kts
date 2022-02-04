plugins {
    id("org.kodein.library.mpp")
}

val coroutinesVersion: String by rootProject.extra

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":mockmp-runtime"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
        }
        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)
    }
}

kodeinUpload {
    name = "mockmp-coroutines"
    description = "MocKMP for coroutines"
}
