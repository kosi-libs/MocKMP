plugins {
    kodein.library.mpp
}

kotlin.kodein {
    all()
    jvm {
        sources.mainDependencies {
            implementation(libs.objenesis)
            implementation(libs.javassist)
        }
    }
}

kotlin.jvmToolchain(17)

kodeinUpload {
    name = "mockmp-runtime"
    description = "MocKMP runtime"
}
