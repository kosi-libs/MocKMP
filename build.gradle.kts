plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.6.10-1.0.4" apply false
}

val kspVersion by extra { "1.6.10-1.0.4" }

allprojects {
    group = "org.kodein.mock"
    version = "1.4.0"
}
