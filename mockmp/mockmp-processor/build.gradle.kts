plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

// Deliberately no dependency on mockmp-runtime: the processor never loads a runtime type. The five
// annotations are matched by fully-qualified name string, because KSP resolves annotations from the
// classpath of the module being processed rather than the processor's own; and Mocker and the
// mockFunctionN helpers are only ever ClassName/MemberName values written into the generated source.
// Depending on the runtime here would put it on every consumer's KSP classpath and in the published
// POM for nothing — the Gradle plugin adds it to the consumer's implementation configuration itself.
dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinPoet.ksp)
}

mavenPublishing {
    pom {
        name = "mockmp-processor"
        description = "MocKMP KSP processor"
    }
}
