import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin.jvmToolchain(11)

android.sourceSets {
    named("main") {
        kotlin.directories.add("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
        dependencies {
            implementation(libs.kotlinx.datetime)
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
afterEvaluate {
    tasks.withType<AbstractTestTask> {
        testLogging {
            events("passed", "skipped", "failed", "standard_out", "standard_error")
            showExceptions = true
            showStackTraces = true
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
    project.tasks.preBuild.configure {
        dependsOn(copySources)
    }
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
