plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.7.0-1.0.6" apply false
}

val kspVersion by extra { "1.7.0-1.0.6" }

allprojects {
    group = "org.kodein.mock"
    version = "1.8.0"
}
