plugins {
    kotlin("multiplatform") version "1.6.0-RC2"
    `maven-publish`
    id("com.google.devtools.ksp") version "1.6.0-RC-1.0.1-RC" apply false
}

val kotlinVersion by extra { "1.6.0-RC2" }
val kspVersion by extra { "1.6.0-RC-1.0.1-RC" }

allprojects {
    group = "org.kodein.micromock"
    version = "0.1"

    repositories {
        mavenCentral()
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    ios()
    iosSimulatorArm64()
    tvos()
    watchos()

    sourceSets {
        val commonMain by getting

        val jvmMain by getting {
            dependencies {
                implementation("org.objenesis:objenesis:3.2")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by getting {
            dependsOn(nativeMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(getByName("iosMain"))
        }

        val tvosMain by getting {
            dependsOn(nativeMain)
        }

        val watchosMain by getting {
            dependsOn(nativeMain)
        }

        all {
            languageSettings.progressiveMode = true
        }
    }
}
