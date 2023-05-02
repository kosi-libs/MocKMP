plugins {
    kodein.library.mpp
}

val copySrc by tasks.creating(Sync::class) {
    from("$projectDir/../mockmp-test-helper/src")
    into("$buildDir/src")
}

kotlin.kodein {
    all {
        compilations.all {
            compileTaskProvider.configure { dependsOn(copySrc) }
        }
    }
    common.main {
        kotlin.srcDir("$buildDir/src/commonMain/kotlin")
        dependencies {
            api(projects.mockmpRuntime)
            implementation(kodeinGlobals.kotlin.test)
        }
    }

    jvm {
        sources.mainDependencies {
            implementation(kodeinGlobals.kotlin.test.junit5)
        }
    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
