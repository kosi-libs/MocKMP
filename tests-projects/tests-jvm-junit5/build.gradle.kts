plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

tasks.test {
    useJUnitPlatform()
}

// Registered before its first use, so the source directories below can be derived from it: a provider
// derived from a task provider carries the task dependency with it, which a plain path string does not.
val copySources = tasks.register<Sync>("copySources") {
    from("$rootDir/tests-mp-junit4/src")
    into(layout.buildDirectory.dir("src"))
}

kotlin.sourceSets {
    main {
        kotlin.srcDir(copySources.map { it.destinationDir.resolve("commonMain/kotlin") })
        dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
        }
    }

    test {
        kotlin.srcDir(copySources.map { it.destinationDir.resolve("commonTest/kotlin") })
        dependencies {
            implementation(libs.kotlin.test.junit5)
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
