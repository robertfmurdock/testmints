import java.time.LocalDate

plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    alias(libs.plugins.com.zegreatrob.tools.fingerprint)
    alias(libs.plugins.com.zegreatrob.tools.dashboard.test.summary)
    base
}

group = "com.zegreatrob.testmints"

dashboardTestSummary {
    includedCounts.from(
        layout.projectDirectory.file("libraries/build/dashboard/build-test-counts.json"),
        layout.projectDirectory.file("plugins/build/dashboard/build-test-counts.json"),
        layout.projectDirectory.file("plugins-test/build/dashboard/build-test-counts.json"),
        layout.projectDirectory.file("convention-plugins/build/dashboard/build-test-counts.json"),
    )
}

tasks.named("dashboardTestSummary") {
    dependsOn(
        gradle.includedBuild("libraries").task(":dashboardTestSummaryCounts"),
        gradle.includedBuild("plugins").task(":dashboardTestSummaryCounts"),
        gradle.includedBuild("plugins-test").task(":dashboardTestSummaryCounts"),
        gradle.includedBuild("convention-plugins").task(":dashboardTestSummaryCounts"),
    )
}

tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

fingerprintConfig {
    includedBuilds = listOf("libraries", "plugins")
}

tasks {
    assemble {
        dependsOn(aggregateFingerprints)
    }
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
    val publish = register("publish") {
        mustRunAfter(check)
        // Pause snapshot publication through Sunday, November 15, 2026.
        val snapshotPublishingPaused = project.version.toString().contains("SNAPSHOT") &&
            providers.provider { LocalDate.now() < LocalDate.of(2026, 11, 16) }.get()
        if (!snapshotPublishingPaused) {
            dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
        }
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
        finalizedBy("dashboardTestSummary")
    }
    clean {
        dependsOn(provider { includedBuilds.map { it.task(":clean") } })
    }
    release {
        mustRunAfter(check, assemble)
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
