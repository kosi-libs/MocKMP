plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvm()
    jvmToolchain(11)

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
        commonMain {
            kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
            dependencies {
                implementation(projects.mockmpRuntime)
                implementation(kotlin("test"))
            }
        }
        jvmMain.dependencies {
            implementation(kotlin("test-junit5"))
        }
    }
}

val copySrc = tasks.register<Sync>("copySrc") {
    from("$projectDir/../mockmp-test-helper/src")
    into("${layout.buildDirectory.get().asFile}/src")
}

afterEvaluate {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>> {
        dependsOn(copySrc)
    }
    tasks.withType<org.gradle.jvm.tasks.Jar> {
        dependsOn(copySrc)
    }
}


mavenPublishing {
    pom {
        name = "mockmp-test-helper-junit5"
        description = "MocKMP test helper with JUnit5"
    }
}
