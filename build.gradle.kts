plugins {
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    `maven-publish`
    signing
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    base
}

group = "com.zegreatrob.testmints"

nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
    }
    val includedBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
        gradle.includedBuild("convention-plugins"),
    )
    "versionCatalogUpdate" {
        dependsOn(provider { includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }

    create<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":collectResults") } })
        from(includedBuilds.map { it.projectDir.resolve("build/test-output") })
        into("${rootProject.buildDir.path}/test-output/${project.path}".replace(":", "/"))
    }

    create("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":formatKotlin") } })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":check") } })
    }
    clean {
        dependsOn(provider { includedBuilds.map { it.task(":clean") } })
    }

    release {
        mustRunAfter(check)
        finalizedBy(provider { (getTasksByName("publish", true)).toList() })
    }

    publish {
        mustRunAfter(check)
        dependsOn(provider { (getTasksByName("publish", true) - this).toList() })
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
    }

    if (isMacRelease()) {
        tag {
            enabled = false
        }
        githubRelease {
            enabled = false
        }
        release {
            enabled = false
        }
    }
}

fun org.ajoberstar.grgit.Commit.extractVersion(): String? {
    val open = fullMessage.indexOf("[")
    val close = fullMessage.indexOf("]")

    if (open < 0 || close < 0) {
        return null
    }
    return fullMessage.subSequence(open + 1, close).toString()
}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")

fun Project.isMacRelease() = findProperty("release-target") == "mac"

fun TaskCollection<AbstractPublishToMaven>.disableTaskForPublication(
    targetPub: MavenPublication
) {
    matching { it.publication == targetPub }
        .configureEach { this.onlyIf { false } }
}

val macTargets = listOf(
    "macosX64",
    "iosX64",
    "iosArm32",
    "iosArm64"
)

fun PublicationContainer.nonMacPublications() = matching { !macTargets.contains(it.name) }

fun PublicationContainer.jvmPublication(): NamedDomainObjectSet<Publication> = matching { it.name == "jvm" }
