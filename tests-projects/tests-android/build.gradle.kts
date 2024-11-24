import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(kodeinGlobals.plugins.android.library)
    alias(kodeinGlobals.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

android {
    namespace = "org.kodein.mock.tests_android"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin.sourceSets {
    main {
        kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
        dependencies {
            implementation(libs.datetime)
        }
    }

    test {
        kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonTest/kotlin")
        dependencies {
            implementation(kodeinGlobals.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(kodeinGlobals.kotlin.test.junit)
        }
    }
}

val copySources = tasks.register<Sync>("copySources") {
    from("$rootDir/tests-mp-junit4/src")
    into("${layout.buildDirectory.get().asFile}/src")
}

afterEvaluate {
    project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
        dependsOn(copySources)
    }
}

mockmp {
    onTest {
        withHelper()
    }
}

// Showing tests in Gradle command line
afterEvaluate {
    tasks.withType<AbstractTestTask> {
        testLogging {
            events("passed", "skipped", "failed", "standard_out", "standard_error")
            showExceptions = true
            showStackTraces = true
        }
    }
}
