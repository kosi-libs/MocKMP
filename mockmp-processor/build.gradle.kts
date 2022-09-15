plugins {
    id("org.kodein.library.jvm")
}

val kspVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation(project(":mockmp-runtime"))
}

kodeinUpload {
    name = "mockmp-processor"
    description = "MocKMP KSP processor"
}
