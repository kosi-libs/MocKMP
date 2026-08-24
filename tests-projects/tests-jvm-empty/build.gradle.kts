// The single-target counterpart of tests-mp-empty: here the accessors come from mockmp.single.kt,
// whose inline mock()/fake() call the very functions the processor generates.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvmToolchain(11)

    sourceSets {
        test {
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
    onTest()
}
