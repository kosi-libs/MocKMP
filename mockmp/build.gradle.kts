plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kosi.publish.module) apply false

    alias(libs.plugins.kosi.publish.root)
}

allprojects {
    group = "org.kodein.mock"
    version = "2.2.0"
}

intermediateProjectTasks()
