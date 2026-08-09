// Every option in the mockmp block below is asserted by this project *compiling* — the same shape as
// tests-mp-empty, which asserts that applying the plugin to an unannotated project is harmless.
//
// That works because each option feeds two independent paths that have to agree, and Kotlin rejects
// them when they do not:
//
//  - public() substitutes {VISIBILITY} into the `expect` declarations extracted from
//    mockmp.multi.kt, while their `actual`s take their visibility from the processor's
//    org.kodein.mock.visibility argument. An expect/actual visibility mismatch does not compile, and
//    neither does the `public inline fun … = mock(T::class)` in that same resource once it reaches a
//    non-public declaration.
//  - accessorsPackage() feeds both {PACKAGE} and org.kodein.mock.package. Disagree, and the `expect`
//    has no `actual`. The test source imports from custom.accessors, which is what pins the name.
//  - targets() selects which targets KSP runs on. Drop one and that target's `actual` goes missing.
//    Two targets, so the filter has something to get wrong.
//  - withHelper(junit5) takes the explicit branch, which skips JUnit autodetection entirely. The two
//    junit5 projects still cover the detected path.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvm {
        // For the runner itself: the helper is bound to JUnit 5 above, so the test task has to be too.
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    jvmToolchain(11)

    js {
        nodejs()
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

mockmp {
    onTest {
        public()
        accessorsPackage("custom.accessors")
        targets("jvm", "js")
        withHelper(junit5)
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
