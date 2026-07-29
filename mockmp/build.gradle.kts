import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.mavenPublish) apply false
}

allprojects {
    group = "org.kodein.mock"
    version = "2.1.0"
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

intermediateProjectTasks()
