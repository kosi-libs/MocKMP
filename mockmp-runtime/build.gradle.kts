plugins {
    id("org.kodein.library.mpp")
}

apply(from = "../maven-publishing-setup.kts")

val kspVersion: String by extra

kodein {
    kotlin {
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation("org.objenesis:objenesis:3.2")
                implementation("org.javassist:javassist:3.28.0-GA")
            }
        }
        add(kodeinTargets.native.all)
        add(kodeinTargets.js.js)
    }
}

kodeinUpload {
    name = "mockmp-runtime"
    description = "MocKMP runtime"
}
