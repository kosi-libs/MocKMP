plugins {
    id("org.kodein.mpp")
    alias(libs.plugins.ksp)
}

kodein {
    kotlin {
        common.main.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
        }

        common.test {
            dependencies {
                implementation(project(":mockmp-runtime"))
                implementation(project(":test-helper:mockmp-test-helper"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")
                implementation(kotlin("test-junit"))
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
