plugins {
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    `maven-publish`
    signing
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    base
}

group = "com.zegreatrob.testmints"

nexusPublishing {
    repositories {
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
    "versionCatalogUpdate" {
        dependsOn(provider { gradle.includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }

    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
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
