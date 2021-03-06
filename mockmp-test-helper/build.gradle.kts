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
                implementation("junit:junit:4.13.2")
            }
        }
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)
    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
