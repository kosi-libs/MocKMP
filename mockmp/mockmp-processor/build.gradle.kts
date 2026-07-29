plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kosi.publish.module)
}

dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.kotlinPoet.ksp)
    implementation(projects.mockmpRuntime)
}

kosiPublish {
    name = "mockmp-processor"
    description = "MocKMP KSP processor"
}
