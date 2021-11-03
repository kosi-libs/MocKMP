plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version "1.6.0-RC-1.0.1-RC"
}

kotlin {
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

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(rootProject)
            }
            // Adding KSP JVM result to COMMON source set
            kotlin.srcDir("build/generated/ksp/jvmTest/kotlin")
        }

        all {
            languageSettings.progressiveMode = true
        }
    }
}

dependencies {
    // Running KSP for JVM only
    "kspJvmTest"(project(":processor"))
}

// Adding KSP JVM as a dependency to all Kotlin compilations
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name.startsWith("compileTestKotlin")) {
        dependsOn("kspTestKotlinJvm")
    }
}
