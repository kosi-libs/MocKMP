plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.kodein.mock.mockmp.test.android"

    compileSdk = 37

    defaultConfig { 
        minSdk = 24
        targetSdk = 37
    }
}

dependencies {
    implementation(projects.testsMpAndroid)
}