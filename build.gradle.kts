plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6" apply false
}

val kspVersion by extra { "1.7.10-1.0.6" }

allprojects {
    group = "org.kodein.mock"
    version = "1.9.0"
}
