package com.zegreatrob.testmints.plugins

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

nexusPublishing {
    this@nexusPublishing.repositories {
        sonatype {
            username.set(providers.environmentVariable("SONATYPE_USERNAME"))
            password.set(providers.environmentVariable("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}
