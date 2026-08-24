plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

// Registered before its first use, so the source directories below can be derived from it: a provider
// derived from a task provider carries the task dependency with it, which a plain path string does not.
val copySources = tasks.register<Sync>("copySources") {
    from("$rootDir/tests-mp-junit4/src")
    into(layout.buildDirectory.dir("src"))
}

kotlin {
    jvmToolchain(11)

    android {
        namespace = "org.kodein.mock.test.mp.android.junit4"
        compileSdk = 36
        minSdk = 24
        withHostTest {}
        lint {}
    }

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
        binaries.library()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.library()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(copySources.map { it.destinationDir.resolve("commonMain/kotlin") })
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.core)
            }
        }
        commonTest {
            kotlin.srcDir(copySources.map { it.destinationDir.resolve("commonTest/kotlin") })
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        // androidHostTest, not androidUnitTest: under com.android.kotlin.multiplatform.library the
        // host-side test compilation is 'hostTest' (enabled by withHostTest above), so its default
        // source set is named after it. androidUnitTest is the name used by the older androidTarget()
        // plugin; declared here it belongs to no compilation, and KGP warns about it as unused.
        // There is no generated accessor for it — the android target comes from AGP, so KGP only
        // supplies accessors for the source sets it names itself, androidUnitTest among them.
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

mockmp {
    onTest {
        withHelper()
    }
}

// Showing tests in Gradle command line
tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events("passed", "skipped", "failed", "standard_out", "standard_error")
        showExceptions = true
        showStackTraces = true
    }
}

// androidPreBuild still needs the dependency stated: it consumes the copied sources without going
// through a source directory, so nothing derives it.
tasks.androidPreBuild.configure {
    dependsOn(copySources)
}
