import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

dependencies {
    implementation(platform(kotlin("bom", embeddedKotlinVersion)))
    implementation(kotlin("stdlib", embeddedKotlinVersion))
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    implementation(libs.com.github.ben.manes.gradle.versions.plugin)
    implementation(libs.org.jmailen.gradle.kotlinter.gradle)
}

tasks {
    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|-beta.*|-Beta.*|-alpha.*)\$"
                .toRegex()
                .matches(candidate.version)
        }
    }
    register("collectResults") {
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
}
