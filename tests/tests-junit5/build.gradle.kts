plugins {
    kodein.mpp
    alias(libs.plugins.ksp)
}

tasks.register<Sync>("copySrc") {
    from("$projectDir/../tests-junit4/src")
    into(layout.buildDirectory.map { "${it.asFile}/src" })
}

kotlin.kodein {
    allTestable {
        compilations.all {
            compileTaskProvider { dependsOn("copySrc") }
        }
        compilations.test {
            compileTaskProvider { dependsOn("kspTestKotlinJvm") }
        }
    }

    common.main {
        kotlin.srcDir(layout.buildDirectory.map { "${it.asFile}/src/commonMain/kotlin" })
        dependencies {
            implementation(libs.datetime)
        }
    }
    common.test {
        kotlin.srcDir(layout.buildDirectory.map { "${it.asFile}/src/commonTest/kotlin" })
        dependencies {
            implementation(projects.mockmpRuntime)
            implementation(projects.testHelper.mockmpTestHelperJunit5)
            implementation(libs.coroutines.test)
        }
        // Adding KSP JVM result to COMMON source set
        kotlin.srcDir("build/generated/ksp/common/commonTest/kotlin")
    }

    jvm {
        target.testRuns.all {
            executionTask.configure { useJUnitPlatform() }
        }
        sources.mainDependencies {
            implementation(kodeinGlobals.kotlin.test.junit5)
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

afterEvaluate {
    tasks.named("kspTestKotlinJvm") { dependsOn("copySrc") }
}