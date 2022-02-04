plugins {
    id("org.kodein.mpp")
    id("com.google.devtools.ksp")
}

kodein {
    kotlin {
        common.test {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":mockmp-runtime"))
                implementation(project(":mockmp-test-helper"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            }
            // Adding KSP JVM result to COMMON source set
            kotlin.srcDir("build/generated/ksp/jvmTest/kotlin")
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)
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
