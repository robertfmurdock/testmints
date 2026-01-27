import nl.littlerobots.vcu.plugin.versionSelector

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

dependencies {
    implementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    implementation(kotlin("stdlib"))
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    implementation(libs.nl.littlerobots.vcu.plugin)
    implementation(libs.org.jmailen.gradle.kotlinter.gradle)
}

tasks {
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
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|.*-beta.*|.*-Beta.*|.*-alpha.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}
