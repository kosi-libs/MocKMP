plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {
        common.main.dependencies {
            api(projects.mockmpRuntime)
            implementation(kodeinGlobals.kotlin.test)
        }
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation(kodeinGlobals.kotlin.test.junit)
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
