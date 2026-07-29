plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    `lifecycle-base`
}

/*
 In a composite build, tasks run from the root will not automatically propagate to subprojects (see
 https://github.com/gradle/gradle/issues/20863).
*/
afterEvaluate {
    listOf("build", "clean", "assemble", "check").forEach { taskName ->
        tasks.named(taskName) {
            dependsOn(subprojects.map { ":${it.path}:$taskName" })
        }
    }
}
