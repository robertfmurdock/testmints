import com.zegreatrob.tools.dashboard.MergeJUnitCounts
import nl.littlerobots.vcu.plugin.versionSelector

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.com.zegreatrob.tools.dashboard.test.summary)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.nl.littlerobots.vcu.plugin)
    implementation(libs.kotlinter.gradle)
    implementation(libs.gradle.nexus.publish.plugin)
}

tasks {
    register("collectResults") {
        group = "verification"
        description = "Collects test reports and results."
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
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
    sortByKey.set(true)
    keep {
        keepUnusedVersions.set(true)
    }
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|.*-beta.*|.*-Beta.*|.*-alpha.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}


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
    outgoing.artifact(layout.buildDirectory.dir("test-output")) { builtBy(tasks.named("collectResults")) }
}
