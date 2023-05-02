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

kodeinUpload {
    name = "mockmp-runtime"
    description = "MocKMP runtime"
}
