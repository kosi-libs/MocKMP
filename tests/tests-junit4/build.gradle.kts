plugins {
    id("org.kodein.mpp")
    alias(libs.plugins.ksp)
}

kodein {
    kotlin {
        common.main.dependencies {
            implementation(libs.datetime)
        }

        common.test {
            dependencies {
                implementation(projects.mockmpRuntime)
                implementation(projects.testHelper.mockmpTestHelper)
                implementation(libs.coroutines.test)
                implementation(kodeinGlobals.kotlin.test.junit)
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
    "kspJvmTest"(projects.mockmpProcessor)
}

// Adding KSP JVM as a dependency to all Kotlin compilations
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name.startsWith("compileTestKotlin")) {
        dependsOn("kspTestKotlinJvm")
    }
}
