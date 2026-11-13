package com.zegreatrob.testmints.plugins

import java.nio.charset.Charset
import java.util.*

plugins {
    id("com.zegreatrob.testmints.plugins.pom-metadata")
    signing
}

group = "com.zegreatrob.testmints"

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


tasks {
    publish { finalizedBy("::closeAndReleaseSonatypeStagingRepository") }

    val javadocJar = register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from("${rootDir.absolutePath}/javadocs")
    }
    publishing.publications {
        jvmPublication().withType<MavenPublication> {
            artifact(javadocJar)
        }
        val publishTasks = withType<AbstractPublishToMaven>()
        if (isMacRelease()) {
            nonMacPublications().withType<MavenPublication> { publishTasks.disableTaskForPublication(this) }
        } else {
            macPublications().withType<MavenPublication> { publishTasks.disableTaskForPublication(this) }
        }
    }
}

fun Project.isMacRelease() = findProperty("release-target") == "mac"

fun TaskCollection<AbstractPublishToMaven>.disableTaskForPublication(
    targetPub: MavenPublication
) {
    matching { it.publication == targetPub }
        .configureEach { this.onlyIf { false } }
}

fun PublicationContainer.macPublications() = matching {
    it.name in listOf(
        "macosArm64",
        "iosArm32",
        "iosArm64"
    )
}

fun PublicationContainer.nonMacPublications() = matching {
    it.name !in listOf(
        "macosArm64",
        "iosArm32",
        "iosArm64"
    )
}

fun PublicationContainer.jvmPublication(): NamedDomainObjectSet<Publication> = matching { it.name == "jvm" }
