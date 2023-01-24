plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {
        common.main {
            kotlin.srcDir("$buildDir/src/commonMain/kotlin")
            dependencies {
                api(projects.mockmpRuntime)
                implementation(kodeinGlobals.kotlin.test)
            }
        }
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation(kodeinGlobals.kotlin.test.junit5)
            }
        }
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)

        val copySrc by tasks.creating(Sync::class) {
            from("$projectDir/../mockmp-test-helper/src")
            into("$buildDir/src")
        }

        targets.all {
            compilations.all {
                compileTaskProvider.configure { dependsOn(copySrc) }
            }
        }

    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
