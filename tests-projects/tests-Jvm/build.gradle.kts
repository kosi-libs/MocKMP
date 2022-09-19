plugins {
    kotlin("multiplatform") version "1.7.20-RC"
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

        val jvmTest by getting {
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
