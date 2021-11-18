plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(BOTH) {
        browser()
        nodejs()
    }

    ios()
    iosSimulatorArm64()
    tvos()
    watchos()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(rootProject)
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        all {
            languageSettings.progressiveMode = true
        }
    }
}
