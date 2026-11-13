package com.zegreatrob.testmints.plugins

import java.nio.charset.Charset
import java.util.Base64

plugins {
    `java-gradle-plugin`
    signing
    id("com.zegreatrob.testmints.plugins.pom-metadata")
}

gradlePlugin {
    website.set("https://github.com/robertfmurdock/testmints")
    vcsUrl.set("https://github.com/robertfmurdock/testmints")
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            gradlePlugin.testSourceSets(sources)
        }
    }
}

group = "com.zegreatrob.testmints"

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

tasks {
    check {
        dependsOn(testing.suites.named("functionalTest"))
    }
    named<Test>("test") {
        useJUnitPlatform()
    }
    named<Test>("functionalTest") {
        environment("ROOT_DIR", rootDir)
        environment("RELEASE_VERSION", rootProject.version)
    }
    val copyTemplates = register<Copy>("copyTemplates") {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"

        from(project.projectDir.resolve("src/main/templates")) {
            filter<org.apache.tools.ant.filters.ReplaceTokens>(
                "tokens" to mapOf("TESTMINTS_BOM_VERSION" to rootProject.version)
            )
        }
        into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
    }
    sourceSets {
        main {
            java.srcDirs(copyTemplates)
        }
    }

    val projectResultPath = project.layout.buildDirectory.dir("test-output/${project.path}/results".replace(":", "/"))

    val check = named("check")
    register<Copy>("collectResults") {
        group = "verification"
        description = "Collects this project's reports and test results."
        mustRunAfter(check)
        from(layout.buildDirectory.dir("reports"))
        from(layout.buildDirectory.dir("test-results"))
        into(projectResultPath)
    }
}

signing {
    val signingKey = project.findProperty("signingKey") as String?
    val signingPassword = project.findProperty("signingPassword") as String?

    if (signingKey != null) {
        val decodedKey = Base64.getDecoder().decode(signingKey).toString(Charset.defaultCharset())
        useInMemoryPgpKeys(
            decodedKey,
            signingPassword
        )
    }
    sign(publishing.publications)
}

if (isMacRelease()) {
    tasks.withType<AbstractPublishToMaven>().configureEach { onlyIf { false } }
}

fun Project.isMacRelease() = findProperty("release-target") == "mac"
