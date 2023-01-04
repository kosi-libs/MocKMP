plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":mockmp-runtime"))
            implementation(kotlin("test"))
        }
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.ir.js)
    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
