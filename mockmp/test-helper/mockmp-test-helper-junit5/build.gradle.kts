plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.mavenPublish)
}

// Registered before its first use, so the source directory below can be derived from it: a provider
// derived from a task provider carries the task dependency with it, which a plain path string does not
// — that is what the afterEvaluate wiring here used to be compensating for.
val copySrc = tasks.register<Sync>("copySrc") {
    from("$projectDir/../mockmp-test-helper/src")
    into(layout.buildDirectory.dir("src"))
}

kotlin {
    jvm()
    jvmToolchain(11)

    iosSimulatorArm64()
    iosArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()
    tvosArm64()

    linuxArm64()
    linuxX64()
    macosArm64()
    mingwX64()

    js {
        browser()
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(copySrc.map { it.destinationDir.resolve("commonMain/kotlin") })
            dependencies {
                implementation(projects.mockmpRuntime)
                implementation(libs.kotlin.test)
            }
        }
        jvmMain.dependencies {
            implementation(libs.kotlin.test.junit5)
        }
    }
}


mavenPublishing {
    pom {
        name = "mockmp-test-helper-junit5"
        description = "MocKMP test helper with JUnit5"
    }
}
