import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    base
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

repositories {
    mavenCentral()
}

nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}

tasks {
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
    }
    publish {
        mustRunAfter(check)
        dependsOn(
            provider { (getTasksByName("publish", true) - this).toList() },
        )
        if (!isSnapshot()) {
            dependsOn(provider { (getTasksByName("publishPlugins", true) - this).toList() })
        }
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
    }
    register<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        from(provider { (getTasksByName("collectResults", true) - this).toList() }.map {
            it.map { task -> task.project }
                .toSet()
                .map { project -> project.layout.buildDirectory.dir("test-output").get() }
        })
        into(rootProject.layout.buildDirectory.dir("test-output/${project.path}".replace(":", "/")))
    }

    register("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
    }
    register("lintKotlin") {
        dependsOn(provider { (getTasksByName("lintKotlin", true) - this).toList() })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
    }
    clean {
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|.*-beta.*|.*-Beta.*|.*-alpha.*)$"
                .toRegex()
                .matches(candidate.version)
        }
    }
}
fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")
