plugins {
    id("org.kodein.mpp")
    alias(libs.plugins.ksp)
}

kodein {
    kotlin {
        common.main {
            kotlin.srcDir("$buildDir/src/commonMain/kotlin")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
            }
        }

        common.test {
            kotlin.srcDir("$buildDir/src/commonTest/kotlin")
            dependencies {
                implementation(project(":mockmp-runtime"))
                implementation(project(":test-helper:mockmp-test-helper-junit5"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")
            }
            // Adding KSP JVM result to COMMON source set
            kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
        }

        add(kodeinTargets.jvm.jvm) {
            target {
                testRuns["test"].executionTask.configure {
                    useJUnitPlatform()
                }
            }
            test.dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
        add(kodeinTargets.js.js)

        val copySrc by tasks.creating(Sync::class) {
            from("$projectDir/../tests-junit4/src")
            into("$buildDir/src")
        }

        targets.all {
            compilations.all {
                compileKotlinTaskProvider.configure { dependsOn(copySrc) }
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
