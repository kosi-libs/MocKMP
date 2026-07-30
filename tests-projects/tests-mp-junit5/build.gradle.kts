import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
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
            kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
            dependencies {
                implementation(libs.kotlinx.datetime)
            }
        }
        commonTest {
            kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonTest/kotlin")
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
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

val copySources = tasks.register<Sync>("copySources") {
    from("$rootDir/tests-mp-junit4/src")
    into("${layout.buildDirectory.get().asFile}/src")
}

afterEvaluate {
    project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
        dependsOn(copySources)
    }
}
