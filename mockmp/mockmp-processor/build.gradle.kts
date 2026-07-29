plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinPoet.ksp)
    implementation(projects.mockmpRuntime)
}

mavenPublishing {
    pom {
        name = "mockmp-processor"
        description = "MocKMP KSP processor"
    }
}
