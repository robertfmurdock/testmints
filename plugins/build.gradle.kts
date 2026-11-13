import com.zegreatrob.tools.dashboard.MergeJUnitCounts
import nl.littlerobots.vcu.plugin.versionSelector

plugins {
    base
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.zegreatrob.tools.fingerprint)
    alias(libs.plugins.com.zegreatrob.tools.dashboard.test.summary)
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
    val closeAndReleaseSonatypeStagingRepository = getByName("closeAndReleaseSonatypeStagingRepository") {
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
    val collectResults = register<Copy>("collectResults") {
        group = "verification"
        description = "Collects test reports and results."
        into(layout.buildDirectory.dir("test-output"))
    }
    subprojects {
        val reportPath = path.trimStart(':').replace(':', '/')
        pluginManager.withPlugin("com.zegreatrob.testmints.plugins.published-gradle-plugin") {
            val projectReports = tasks.named<Copy>("collectResults")
            collectResults.configure {
                from(projectReports.map { it.destinationDir }) { into("$reportPath/results") }
            }
        }
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
}

versionCatalogUpdate {
    val rejectRegex = "^.*[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|.*-beta.*|.*-Beta.*|.*-alpha.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}

fun Project.isSnapshot() = version.toString().contains("SNAPSHOT")


group = "com.zegreatrob.testmints.dashboard"

configurations.consumable("dashboardBuildTestCountsElements") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named("dashboard-build-test-counts"))
    }
    outgoing.artifact(tasks.named<MergeJUnitCounts>("collectDashboardBuildTestCounts").flatMap { it.countsFile })
}

configurations.consumable("collectedReportsElements") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named("collected-test-reports"))
    }
    outgoing.artifact(tasks.named<Copy>("collectResults").map { it.destinationDir })
}
