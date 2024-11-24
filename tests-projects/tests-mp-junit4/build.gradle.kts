plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvm()

    iosSimulatorArm64()
    iosX64()

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.datetime)
            }
        }
        commonTest {
            dependencies {
                implementation(kodeinGlobals.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(kodeinGlobals.kotlin.test.junit)
            }
        }
    }
}

mockmp {
    onTest {
        withHelper()
    }
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
