plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    base
}

group = "com.zegreatrob.testmints"

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    val librariesBuild = gradle.includedBuild("libraries")
    val publishableBuilds = listOf(
        librariesBuild,
        gradle.includedBuild("plugins"),
    )
    val pluginsTestBuild = gradle.includedBuild("plugins-test")
    val includedBuilds = publishableBuilds + listOf(
        gradle.includedBuild("convention-plugins"),
        pluginsTestBuild
    )
    val publish by registering {
        mustRunAfter(check)
        dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
    }
    "versionCatalogUpdate" {
        dependsOn(provider { includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }
    register("kotlinUpgradeYarnLock") {
        dependsOn(provider {
            librariesBuild.task(":kotlinUpgradeYarnLock")
            pluginsTestBuild.task(":kotlinUpgradeYarnLock")
        })
    }
    register<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":collectResults") } })
        from(includedBuilds.map { it.projectDir.resolve("build/test-output") })
        into(rootProject.layout.buildDirectory.dir("test-output/${project.path}".replace(":", "/")))
    }
    register("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":formatKotlin") } })
    }
    register("lintKotlin") {
        dependsOn(provider { (getTasksByName("lintKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":lintKotlin") } })
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
        finalizedBy(publish, currentContributionData)
    }
    currentContributionData {
        mustRunAfter(tag)
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

fun Project.isMacRelease() = findProperty("release-target") == "mac"
