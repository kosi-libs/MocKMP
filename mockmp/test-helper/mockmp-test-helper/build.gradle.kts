plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kosi.publish.module)
}

kotlin {
    jvm()
    jvmToolchain(17)

    iosSimulatorArm64()
    iosArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()
    tvosArm64()

    linuxArm64()
    linuxX64()
    macosArm64()
    mingwX64()

    js {
        browser()
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.mockmpRuntime)
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}

kosiPublish {
    name = "mockmp-test-helper"
    description = "MocKMP test helper"
}
