plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {
        common.main {
            kotlin.srcDir("$buildDir/src/commonMain/kotlin")
            dependencies {
                api(project(":mockmp-runtime"))
                implementation(kotlin("test"))
            }
        }
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation(kotlin("test-junit5"))
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
                compileKotlinTaskProvider.configure { dependsOn(copySrc) }
            }
        }

    }
}

kodeinUpload {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
