plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.6.21-1.0.5" apply false
}

val kspVersion by extra { "1.6.21-1.0.5" }

allprojects {
    group = "org.kodein.mock"
    version = "1.6.0"
}
