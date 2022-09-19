plugins {
    id("org.kodein.root")
    id("com.google.devtools.ksp") version "1.7.20-RC-1.0.6" apply false
}

val kspVersion by extra { "1.7.20-RC-1.0.6" }

allprojects {
    group = "org.kodein.mock"
    version = "1.10.0-kotlin-1.7.20-RC"
}
