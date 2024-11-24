import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
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
            kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
            dependencies {
                implementation(libs.datetime)
            }
        }
        commonTest {
            kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonTest/kotlin")
            dependencies {
                implementation(kodeinGlobals.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(kodeinGlobals.kotlin.test.junit5)
            }
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
