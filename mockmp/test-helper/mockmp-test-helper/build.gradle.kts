// Two helper modules exist because the JUnit binding is fixed at *compile* time, not resolved at the
// consumer: kotlin.test.BeforeTest is a typealias, so `javap` on this module's ITestsWithMocks shows
// Lorg/junit/Before; where mockmp-test-helper-junit5's shows Lorg/junit/jupiter/api/BeforeEach;. That
// is also why the Gradle plugin has to detect the JUnit version — neither can be collapsed away by
// depending on plain kotlin-test.
//
// The cost is that both modules publish the same fully-qualified class names (mockmp-test-helper-junit5
// compiles these very sources via a Sync), so they must never both be resolved onto one classpath: the
// plugin picks exactly one, and a build that adds the other by hand gets an undefined result rather
// than an error.
//
// Three ways out have been tried or researched, and all are closed:
//
//  1. Collapse into one module. Impossible, per the typealias above: the JVM part must be compiled
//     against one binding or the other, and depending on plain kotlin-test would not compile.
//
//  2. A shared Gradle capability across the two modules, so that resolving both is a reported conflict.
//     Works for project dependencies, but breaks published ones. Declaring a capability suppresses the
//     implicit one, which differs between a project (mockmp-test-helper) and a published multiplatform
//     module (mockmp-test-helper-jvm); re-declare both and a *single* helper dependency stops resolving,
//     because the KMP root's available-at redirect advertises the shared capability alongside the
//     platform module it points at, so Gradle sees two components claiming it. Verified by publishing to
//     a local repository and resolving from a separate consumer.
//
//  3. The shape kotlin-test itself uses. It is not "one module with per-framework variants" — it is a
//     façade over separate modules: kotlin-test's jvmJUnitApiElements carries kotlin-test-framework +
//     -junit and redirects (available-at) to kotlin-test-junit, whose own variants carry the *disjoint*
//     kotlin-test-junit + kotlin-test-framework-impl. Disjoint is what avoids the clash in 2. Those
//     redirect variants are hand-authored Gradle Module Metadata; KGP exposes no way to add an
//     available-at variant whose capabilities differ from its target's. That façade also has a real
//     plain jvmApiElements jar, which this module cannot have: ITestsWithMocks uses @BeforeTest, so it
//     does not compile for JVM without a binding.
//
// The one patch-sized option, renaming this module's classes so the FQNs stop colliding, is rejected on
// purpose: it would break the property that the same test source compiles against either helper, which
// is the only reason the duplication is worth tolerating.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvm()
    jvmToolchain(11)

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
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(projects.mockmpRuntime)
            api(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.kotlin.test.junit)
        }
    }
}

mavenPublishing {
    pom {
        name = "mockmp-test-helper"
        description = "MocKMP test helper"
    }
}