plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

    `lifecycle-base`
}

/*
 In a composite build, tasks run from the root will not automatically propagate to subprojects (see https://github.com/gradle/gradle/issues/20863).
 This plugin is for root build scripts in composite builds.
 It adds tasks that are present on subprojects, to emulate typical Gradle behavior.
*/
fun Project.applyIntermediate() {
    if (childProjects.isEmpty()) {
        return
    }
    childProjects.values.forEach { child ->
        child.applyIntermediate()
    }
    childProjects.values
        .flatMap { it.tasks.map { it.name to it.group } }
        .distinctBy { (taskName, _) -> taskName }
        .forEach { (taskName, taskGroup) ->
            val childProjectsWithTask = childProjects.values.filter { taskName in it.tasks.names }
            if (taskName !in tasks.names) {
                tasks.register(taskName) { group = taskGroup }
            }
            tasks.named(taskName) {
                dependsOn(childProjectsWithTask.map { it.tasks.named(taskName) })
            }
        }
}
gradle.projectsEvaluated { applyIntermediate() }
