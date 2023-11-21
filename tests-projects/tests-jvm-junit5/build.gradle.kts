plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
    id("org.kodein.mock.mockmp")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    iosSimulatorArm64()
    iosX64()

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("$rootDir/../../tests/tests-junit4/src/commonMain/kotlin")
            dependencies {
                implementation(libs.datetime)
            }
        }
        commonTest {
            kotlin.srcDir("$rootDir/../../tests/tests-junit4/src/commonTest/kotlin")
            dependencies {
                implementation(libs.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kodeinGlobals.kotlin.test.junit5)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kodeinGlobals.kotlin.test)
            }
        }
    }
}

mockmp {
    usesHelper = true
    installWorkaround()
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
