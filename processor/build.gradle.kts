plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.0-RC-1.0.1-RC")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
