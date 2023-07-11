plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    base
}

group = "com.zegreatrob.testmints"


tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    val publishableBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    )
    val includedBuilds = publishableBuilds + gradle.includedBuild("convention-plugins")

    val publish by creating {
        mustRunAfter(check)
        dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
    }

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
        finalizedBy(publish)
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
