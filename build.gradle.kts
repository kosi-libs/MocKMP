plugins {
    kotlin("multiplatform") version "1.6.0-RC"
    `maven-publish`
}

group = "org.kodein.mocking"
version = "1.0-SNAPSHOT"

allprojects {
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
    tvos()
    watchos()

    sourceSets {
        val commonMain by getting

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by getting {
            dependsOn(nativeMain)
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
