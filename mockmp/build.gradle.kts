import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.mavenPublish) apply false

    `lifecycle-base`
}

allprojects {
    group = "org.kodein.mock"
    version = "3.4.0-kt2.3"
}

val mavenPublishPluginId = libs.plugins.mavenPublish.get().pluginId
subprojects {
    pluginManager.withPlugin(mavenPublishPluginId) {
        extensions.configure<MavenPublishBaseExtension> {
            pom {
                url = "https://github.com/kosi-libs/MocKMP"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                        distribution = "https://opensource.org/licenses/MIT"
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/kosi-libs/MocKMP/issues")
                }
                scm {
                    connection.set("https://github.com/kosi-libs/MocKMP.git")
                    url.set("https://github.com/kosi-libs/MocKMP")
                }
                developers {
                    developer {
                        name = "Kodein Koders"
                        email = "contact@kodein.net"
                        url = "https://kodein-koders.com"
                    }
                }
            }
        }
    }
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
