plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
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
        binaries.library()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.library()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}

mockmp {
    onMain()
}

// Showing tests in Gradle command line
afterEvaluate {
    tasks.withType<AbstractTestTask> {
        testLogging {
            events("passed", "skipped", "failed", "standard_out", "standard_error")
            showExceptions = true
            showStackTraces = true
        }
    }
}
