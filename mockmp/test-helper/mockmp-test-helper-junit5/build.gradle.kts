// Compiles mockmp-test-helper's sources verbatim (see copySrc below), against kotlin-test-junit5
// rather than kotlin-test-junit. That is the whole difference, and it cannot be deferred to the
// consumer: kotlin.test.BeforeTest is a compile-time typealias, so this module's ITestsWithMocks
// carries Lorg/junit/jupiter/api/BeforeEach; where the other's carries Lorg/junit/Before;.
//
// It therefore publishes the same fully-qualified class names as mockmp-test-helper, and the two must
// never both be resolved onto one classpath. See mockmp-test-helper/build.gradle.kts for why that
// cannot be turned into a detectable conflict — three routes were tried or researched and all are
// closed there, with the evidence.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.mavenPublish)
}

// Registered before its first use, so the source directory below can be derived from it: a provider
// derived from a task provider carries the task dependency with it, which a plain path string does not
// — that is what the afterEvaluate wiring here used to be compensating for.
val copySrc = tasks.register<Sync>("copySrc") {
    from("$projectDir/../mockmp-test-helper/src")
    into(layout.buildDirectory.dir("src"))
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

    explicitApi()

    sourceSets {
        commonMain {
            kotlin.srcDir(copySrc.map { it.destinationDir.resolve("commonMain/kotlin") })
            dependencies {
                api(projects.mockmpRuntime)
                api(libs.kotlin.test)
            }
        }
        jvmMain.dependencies {
            implementation(libs.kotlin.test.junit5)
        }
    }
}


mavenPublishing {
    pom {
        name = "mockmp-test-helper-junit5"
        description = "MocKMP test helper with JUnit5"
    }
}
