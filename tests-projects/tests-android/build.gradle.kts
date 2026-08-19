plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

val copySources = tasks.register<Sync>("copySources") {
    from("$rootDir/tests-mp-junit4/src")
    into(layout.buildDirectory.dir("src"))
}

kotlin.jvmToolchain(11)

android.sourceSets {
    named("main") {
        kotlin.directories.add("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
        dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
    named("test") {
        kotlin.directories.add("${layout.buildDirectory.get().asFile}/src/commonTest/kotlin")
        dependencies {
            implementation(libs.kotlin.test.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
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

// AGP's sourceSets DSL takes plain paths, not providers, so the dependency on the copy task cannot
// be derived from the source directory the way the Kotlin projects do it, and preBuild has to state
// it. That is also why the path below stays a string.
tasks.preBuild.configure {
    dependsOn(copySources)
}

android {
    namespace = "org.kodein.mock.tests_android"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
