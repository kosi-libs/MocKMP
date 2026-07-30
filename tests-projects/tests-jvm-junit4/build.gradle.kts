import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin.sourceSets {
    main {
        kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonMain/kotlin")
        dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }

    test {
        kotlin.srcDir("${layout.buildDirectory.get().asFile}/src/commonTest/kotlin")
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
}
