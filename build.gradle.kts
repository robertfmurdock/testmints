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

val dashboardBuildTestCounts = configurations.resolvable("dashboardBuildTestCounts") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named("dashboard-build-test-counts"))
    }
}
val dashboardBuildTestCountsDependencies = configurations.dependencyScope("dashboardBuildTestCountsDependencies")
dashboardBuildTestCounts.configure {
    extendsFrom(dashboardBuildTestCountsDependencies.get())
}

val collectedReports = configurations.resolvable("collectedReports") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named("collected-test-reports"))
    }
}
val collectedReportsDependencies = configurations.dependencyScope("collectedReportsDependencies")
collectedReports.configure {
    extendsFrom(collectedReportsDependencies.get())
}

dependencies {
    add("dashboardBuildTestCountsDependencies", "com.zegreatrob.testmints:testmints:1")
    add("collectedReportsDependencies", "com.zegreatrob.testmints:testmints:1")
    add("dashboardBuildTestCountsDependencies", "com.zegreatrob.testmints.dashboard:plugins:1")
    add("collectedReportsDependencies", "com.zegreatrob.testmints.dashboard:plugins:1")
    add("dashboardBuildTestCountsDependencies", "com.zegreatrob.testmints.dashboard:plugins-test:1")
    add("collectedReportsDependencies", "com.zegreatrob.testmints.dashboard:plugins-test:1")
    add("dashboardBuildTestCountsDependencies", "com.zegreatrob.testmints.dashboard:convention-plugins:1")
    add("collectedReportsDependencies", "com.zegreatrob.testmints.dashboard:convention-plugins:1")
}

dashboardTestSummary {
    includedCounts.from(dashboardBuildTestCounts)
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
        group = "publishing"
        description = "Publishes the libraries and Gradle plugins."
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
        group = "build setup"
        description = "Updates Yarn lockfiles for the included Kotlin builds."
        dependsOn(
            librariesBuild.task(":kotlinUpgradeYarnLock"),
            pluginsTestBuild.task(":kotlinUpgradeYarnLock"),
        )
    }
    register<Copy>("collectResults") {
        group = "verification"
        description = "Collects test reports and results."
        from(collectedReports)
        into(rootProject.layout.buildDirectory.dir("test-output/${project.path}".replace(":", "/")))
    }
    register("formatKotlin") {
        group = "formatting"
        description = "Formats Kotlin sources."
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":formatKotlin") } })
    }
    register("lintKotlin") {
        group = "verification"
        description = "Checks Kotlin source formatting."
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
