plugins {
    id("org.kodein.library.mpp")
}

val kspVersion: String by extra

kodein {
    kotlin {
        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation("org.objenesis:objenesis:3.2")
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

// https://youtrack.jetbrains.com/issue/KT-46257
afterEvaluate {
    println(kotlin.targets["metadata"].compilations.joinToString { it.name })
    val compilation = kotlin.targets["metadata"].compilations["allNativeMain"]
    compilation.compileKotlinTask.doFirst {
        compilation.compileDependencyFiles = files(
            compilation.compileDependencyFiles.filterNot {
                it.absolutePath.endsWith("klib/common/stdlib") || it.absolutePath.endsWith("klib\\common\\stdlib")
            }
        )
    }
}
