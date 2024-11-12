plugins {
    kodein.mpp
    alias(libs.plugins.ksp)
}

kotlin.kodein {
    allTestable {
        compilations.test {
            compileTaskProvider { dependsOn("kspTestKotlinJvm") }
        }
    }

    common.mainDependencies {
        implementation(libs.datetime)
    }
    common.test {
        dependencies {
            implementation(projects.mockmpRuntime)
            implementation(projects.testHelper.mockmpTestHelper)
            implementation(libs.coroutines.test)
        }
        // Adding KSP JVM result to COMMON source set
        kotlin.srcDir("build/generated/ksp/common/commonTest/kotlin")
    }

    jvm {
        sources.mainDependencies {
            implementation(kodeinGlobals.kotlin.test.junit)
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
