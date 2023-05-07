plugins {
    alias(kodeinGlobals.plugins.android.library)
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
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

    defaultConfig {
        minSdk = 21
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    android()
    ios()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("$rootDir/../../tests/tests-junit4/src/commonMain/kotlin")
            dependencies {
                implementation(libs.datetime)
            }
        }
        val commonTest by getting {
            kotlin.srcDir("$rootDir/../../tests/tests-junit4/src/commonTest/kotlin")
            dependencies {
                implementation(libs.coroutines.test)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kodeinGlobals.kotlin.test.junit)
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
