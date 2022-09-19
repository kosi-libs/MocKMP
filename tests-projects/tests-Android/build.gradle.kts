plugins {
    id("com.android.library") version "7.3.0-beta01"
    kotlin("multiplatform") version "1.7.20-RC"
    id("org.kodein.mock.mockmp")
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

android {
    namespace = "org.kodein.mock.tests_android"
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    android()
    ios()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("$rootDir/../tests/src/commonMain/kotlin")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
            }
        }
        val commonTest by getting {
            kotlin.srcDir("$rootDir/../tests/src/commonTest/kotlin")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

mockmp {
    usesHelper = true
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
