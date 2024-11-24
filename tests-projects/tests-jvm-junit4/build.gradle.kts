import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(kodeinGlobals.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
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
