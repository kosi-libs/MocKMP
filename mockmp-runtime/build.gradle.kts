plugins {
    kodein.library.mpp
}

kotlin.kodein {
    all()
    jvm {
        sources.mainDependencies {
            implementation(libs.objenesis)
            implementation(libs.javassist)
            implementation(libs.reflect)
        }
    }
}

kodeinUpload {
    name = "mockmp-runtime"
    description = "MocKMP runtime"
}
