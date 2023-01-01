plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.8.0-1.0.8" apply false
}

val kspVersion by extra { "1.8.0-1.0.8" }

allprojects {
    group = "org.kodein.mock"
    version = "1.12.0"
}
