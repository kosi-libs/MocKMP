plugins {
    alias(kodeinGlobals.plugins.kotlin.multiplatform) apply false
    alias(kodeinGlobals.plugins.kotlin.jvm) apply false
    alias(kodeinGlobals.plugins.kotlin.android) apply false
    alias(kodeinGlobals.plugins.android.library) apply false
}

/*
 In a composite build, tasks run from the root will not automatically propagate to subprojects (see
 https://github.com/gradle/gradle/issues/20863).
*/
apply { plugin("org.gradle.lifecycle-base") }
afterEvaluate {
    listOf("build", "clean", "assemble", "check").forEach { taskName ->
        tasks.named(taskName) {
            dependsOn(subprojects.map { ":${it.path}:$taskName" })
        }
    }
}
