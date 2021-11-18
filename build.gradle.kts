plugins {
    kotlin("multiplatform") version "1.6.0"
    `maven-publish`
    id("com.google.devtools.ksp") version "1.6.0-1.0.1" apply false

    id("org.ajoberstar.git-publish") version "3.0.0"
    id("org.ajoberstar.grgit") version "4.1.0"
}

val kspVersion by extra { "1.6.0-1.0.1" }

allprojects {
    group = "org.kodein.micromock"
    version = "0.5.0"

    repositories {
        mavenCentral()
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(BOTH) {
        browser()
        nodejs()
    }

    ios()
    iosSimulatorArm64()
    tvos()
    watchos()

    sourceSets {
        val commonMain by getting

        val jvmMain by getting {
            dependencies {
                implementation("org.objenesis:objenesis:3.2")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by getting {
            dependsOn(nativeMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(getByName("iosMain"))
        }

        val tvosMain by getting {
            dependsOn(nativeMain)
        }

        val watchosMain by getting {
            dependsOn(nativeMain)
        }

        all {
            languageSettings.progressiveMode = true
        }
    }
}

tasks.create<Sync>("copyMavenLocalArtifacts") {
    group = "publishing"
    dependsOn(":publishToMavenLocal", ":micro-mock-processor:publishToMavenLocal", ":micro-mock-gradle-plugin:publishToMavenLocal")

    val userHome = System.getProperty("user.home")
    val groupDir = project.group.toString().replace('.', '/')
    val localRepository = "$userHome/.m2/repository/$groupDir/"

    from(localRepository) {
        include("*/${project.version}/**")
    }

    into("$buildDir/mvn-repo/$groupDir/")
}

val gitUser = System.getenv("GIT_USER")
val gitPassword = System.getenv("GIT_PASSWORD")
if (gitUser != null && gitPassword != null) {
    System.setProperty("org.ajoberstar.grgit.auth.username", gitUser)
    System.setProperty("org.ajoberstar.grgit.auth.password", gitPassword)
}

gitPublish {
    repoUri.set("https://github.com/Kodein-Framework/Micro-Mock.git")
    branch.set("mvn-repo")
    contents.from("$buildDir/mvn-repo")
    preserve { include("**") }
    val head = grgit.head()
    commitMessage.set("${head.abbreviatedId}: ${project.version} : ${head.fullMessage}")
}
tasks["gitPublishCopy"].dependsOn("copyMavenLocalArtifacts")
