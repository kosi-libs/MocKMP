plugins {
    kodein.library.jvm
}

dependencies {
    implementation(libs.ksp.symbolProcessingApi)
    implementation(libs.ksp.symbolProcessing)
    implementation(libs.kotlinPoet.ksp)
    implementation(projects.mockmpRuntime)
}

kodeinUpload {
    name = "mockmp-processor"
    description = "MocKMP KSP processor"
}
