plugins {
    id("org.kodein.library.jvm")
}

val kspVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation(project(":mockmp-runtime"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}

kodeinUpload {
    name = "mockmp-processor"
    description = "MocKMP KSP processor"
}
