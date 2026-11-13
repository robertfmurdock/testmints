package com.zegreatrob.testmints.plugins

plugins {
    `maven-publish`
}

publishing.publications.withType<MavenPublication>().configureEach {
    val scmUrl = "https://github.com/robertfmurdock/testmints"

    pom.name.set(project.name)
    pom.description.set(project.name)
    pom.url.set(scmUrl)

    pom.licenses {
        license {
            name.set("MIT License")
            url.set(scmUrl)
            distribution.set("repo")
        }
    }
    pom.developers {
        developer {
            id.set("robertfmurdock")
            name.set("Rob Murdock")
            email.set("rob@continuousexcellence.io")
        }
    }
    pom.scm {
        url.set(scmUrl)
        connection.set("git@github.com:robertfmurdock/testmints.git")
        developerConnection.set("git@github.com:robertfmurdock/testmints.git")
    }
}
