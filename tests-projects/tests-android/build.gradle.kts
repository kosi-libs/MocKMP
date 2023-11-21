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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
               jvmTarget = "1.8"
            }
        }
    }

    iosSimulatorArm64()
    iosX64()

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
