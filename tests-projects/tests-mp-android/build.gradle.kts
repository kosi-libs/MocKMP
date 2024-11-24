import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
    alias(kodeinGlobals.plugins.android.library)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvm()
    androidTarget()
    jvmToolchain(17)

    iosSimulatorArm64()
    iosX64()

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
                implementation(kodeinGlobals.kotlin.test.junit)
            }
        }
        androidUnitTest {
            dependencies {
                implementation(kodeinGlobals.kotlin.test.junit)
            }
        }
    }
}

android {
    namespace = "org.kodein.mock.test.mp.android.junit4"

    defaultConfig {
        namespace = "com.example.myapplication"
        minSdk = 24
        compileSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
