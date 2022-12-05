plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.7.22-1.0.8" apply false
}

val kspVersion by extra { "1.7.22-1.0.8" }

allprojects {
    group = "org.kodein.mock"
    version = "1.11.0"
}
