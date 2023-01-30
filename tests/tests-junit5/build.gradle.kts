plugins {
    id("org.kodein.mpp")
    alias(libs.plugins.ksp)
}

kodein {
    kotlin {
        common.main {
            kotlin.srcDir("$buildDir/src/commonMain/kotlin")
            dependencies {
                implementation(libs.datetime)
            }
        }

        common.test {
            kotlin.srcDir("$buildDir/src/commonTest/kotlin")
            dependencies {
                implementation(projects.mockmpRuntime)
                implementation(projects.testHelper.mockmpTestHelperJunit5)
                implementation(libs.coroutines.test)
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
                implementation(kodeinGlobals.kotlin.test.junit5)
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
    "kspJvmTest"(projects.mockmpProcessor)
}

// Adding KSP JVM as a dependency to all Kotlin compilations
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name.startsWith("compileTestKotlin")) {
        dependsOn("kspTestKotlinJvm")
    }
}
