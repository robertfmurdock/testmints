import java.nio.charset.Charset
import java.util.Base64

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
    base
//    id("com.zegreatrob.testmints.plugins.versioning")
//    id("com.zegreatrob.testmints.plugins.reports")
    id("org.jetbrains.kotlin.jvm") version(embeddedKotlinVersion)
    signing
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

testing {
    suites {
        register("functionalTest", JvmTestSuite::class) {
            gradlePlugin.testSourceSets(sources)
        }
    }
}

dependencies {
    implementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    implementation(kotlin("gradle-plugin", libs.versions.org.jetbrains.kotlin.get()))
    implementation(kotlin("test", libs.versions.org.jetbrains.kotlin.get()))

    "functionalTestImplementation"(platform(libs.org.junit.junit.bom))
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
    val copyTemplates by registering(Copy::class) {
        inputs.property("version", rootProject.version)
        filteringCharset = "UTF-8"

        from(project.projectDir.resolve("src/main/templates")) {
            filter<org.apache.tools.ant.filters.ReplaceTokens>(
                "tokens" to mapOf("TESTMINTS_BOM_VERSION" to rootProject.version,)
            )
        }
        into(project.buildDir.resolve("generated-sources/templates/kotlin/main"))
    }
    compileKotlin {
        dependsOn(copyTemplates)
    }
    "compileFunctionalTestKotlin" {
        dependsOn(compileKotlin)
    }
    sourceSets {
        main {
            java.srcDirs(copyTemplates)
        }
    }

    val projectResultPath = "${rootProject.buildDir.path}/test-output/${project.path}/results".replace(":", "/")

    val check by getting
    val copyReportsToRootDirectory by creating(Copy::class) {
        mustRunAfter(check)
        from("build/reports")
        into(projectResultPath)
    }
    val copyTestResultsToRootDirectory by creating(Copy::class) {
        mustRunAfter(check)
        from("build/test-results")
        into(projectResultPath)
    }
    register("collectResults") {
        dependsOn(copyReportsToRootDirectory, copyTestResultsToRootDirectory)
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

    publishing.publications {
        if (isMacRelease()) {
            withType<MavenPublication> {
                tasks.withType<AbstractPublishToMaven>().configureEach { onlyIf { false } }
            }
        }
    }
}

fun Project.isMacRelease() = findProperty("release-target") == "mac"
