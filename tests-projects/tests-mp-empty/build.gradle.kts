// Applies MocKMP but annotates nothing: the plugin must not break a build on its own.
// Multiplatform, so the accessors come from mockmp.multi.kt as `expect` declarations that the
// processor has to provide `actual`s for. A single jvm() target is enough to take that branch.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    id("org.kodein.mock.mockmp")
}

kotlin {
    jvm()
    jvmToolchain(11)

    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
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
