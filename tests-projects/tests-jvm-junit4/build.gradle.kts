plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform)
    id("org.kodein.mock.mockmp")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
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

        val jvmTest by getting {
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
