plugins {
    kodein.library.mpp
}

kotlin.kodein {
    all()
    common.mainDependencies {
        api(projects.mockmpRuntime)
        implementation(kodeinGlobals.kotlin.test)
    }
    jvm {
        sources.mainDependencies {
            implementation(kodeinGlobals.kotlin.test.junit)
        }
    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
