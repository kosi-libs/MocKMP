plugins {
    id("org.kodein.mpp")
    id("com.google.devtools.ksp")
}

kodein {
    kotlin {
        common.main.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
        }

        common.test {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":mockmp-runtime"))
                implementation(project(":mockmp-test-helper"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            }
            // Adding KSP JVM result to COMMON source set
            kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
        add(kodeinTargets.js.js)

        targets.all {
            compilations.all {
                kotlinOptions {
                    allWarningsAsErrors = true
                }
            }
        }
        sourceSets.all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

ksp {
    arg("org.kodein.mock.errors", "throw")
}

dependencies {
    // Running KSP for JVM only
    "kspJvmTest"(project(":mockmp-processor"))
}

// Adding KSP JVM as a dependency to all Kotlin compilations
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name.startsWith("compileTestKotlin")) {
        dependsOn("kspTestKotlinJvm")
    }
}
