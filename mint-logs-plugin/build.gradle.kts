import java.nio.charset.Charset
import java.util.Base64

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
    base
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.reports")
    id("org.jetbrains.kotlin.jvm")
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", libs.versions.org.jetbrains.kotlin.get()))
    implementation(kotlin("test", libs.versions.org.jetbrains.kotlin.get()))
}

group = "com.zegreatrob.testmints"

testing {
    suites {
        register("functionalTest", JvmTestSuite::class) {
            gradlePlugin.testSourceSets(sources)
        }
    }
}

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
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null) {
        val decodedKey = Base64.getDecoder().decode(signingKey).toString(Charset.defaultCharset())
        useInMemoryPgpKeys(
            decodedKey,
            signingPassword
        )
    }
    sign(publishing.publications)
}

afterEvaluate {
    publishing.publications.withType<MavenPublication>().forEach {
        with(it) {
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
                    email.set("robert.f.murdock@gmail.com")
                }
            }
            pom.scm {
                url.set(scmUrl)
                connection.set("git@github.com:robertfmurdock/testmints.git")
                developerConnection.set("git@github.com:robertfmurdock/testmints.git")
            }
        }
    }
}
