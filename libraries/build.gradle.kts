import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec

plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    id("com.zegreatrob.testmints.plugins.versioning")
    alias(libs.plugins.com.zegreatrob.tools.fingerprint)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    signing
    base
}

group = "com.zegreatrob.testmints"

repositories {
    mavenCentral()
}

val kotlinVersion = libs.versions.org.jetbrains.kotlin.get()
val coroutinesVersion = libs.versions.org.jetbrains.kotlinx.coroutines.get()
val serializationVersion = libs.versions.org.jetbrains.kotlinx.serialization.get()
val kotlinLoggingVersion = libs.versions.io.github.oshai.kotlin.logging.get()
val testBalloonVersion = libs.versions.de.infix.testballoon.get()
val slf4jVersion = libs.versions.org.slf4j.get()
val junitVersion = libs.versions.org.junit.get()

allprojects {
    configurations.configureEach {
        if (isCanBeResolved) {
            resolutionStrategy.force(
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion",
                "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion",
                "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion",
                "org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion",
                "io.github.oshai:kotlin-logging:$kotlinLoggingVersion",
                "de.infix.testBalloon:testBalloon-framework-core:$testBalloonVersion",
                "org.slf4j:slf4j-simple:$slf4jVersion",
            )
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(kotlinVersion)
                }
                if (requested.group == "org.junit.jupiter") {
                    useVersion(junitVersion)
                }
                if (requested.group == "org.jetbrains.kotlinx") {
                    if (requested.name.startsWith("kotlinx-coroutines")) {
                        useVersion(coroutinesVersion)
                    }
                    if (requested.name.startsWith("kotlinx-serialization")) {
                        useVersion(serializationVersion)
                    }
                }
            }
        }
    }
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

fingerprintConfig {
    includedProjects = setOf(
        "minassert",
        "standard",
        "async",
        "action",
        "action-async",
        "action-annotation",
        "action-processor",
        "minspy",
        "mindiff",
        "report",
        "mint-logs",
    )
}

tasks {
    val closeAndReleaseSonatypeStagingRepository by getting {
        mustRunAfter(publish)
    }
    publish {
        mustRunAfter(check)
        dependsOn(provider { (getTasksByName("publish", true) - this).toList() })
        finalizedBy(closeAndReleaseSonatypeStagingRepository)
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

rootProject.extensions.findByType(NodeJsEnvSpec::class.java).let {
    it?.version = "23.9.0"
}
