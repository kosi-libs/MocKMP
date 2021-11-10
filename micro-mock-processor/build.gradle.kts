plugins {
    kotlin("jvm")
    `maven-publish`
}

val kspVersion: String by rootProject.extra

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation(rootProject)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

publishing.publications.create<MavenPublication>("processor") {
    from(components["java"])
}
