import java.nio.charset.Charset
import java.util.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
    base
    id("org.jetbrains.kotlin.jvm") version(embeddedKotlinVersion)
    signing
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

gradlePlugin {
    website.set("https://github.com/robertfmurdock/testmints")
    vcsUrl.set("https://github.com/robertfmurdock/testmints")
    plugins {
        named("com.zegreatrob.testmints.action-mint") {
            displayName = "Action-Mint Plugin"
            description = "This plugin will generate boilerplate for using 'actions' easily."
            tags.addAll("action", "domain-driven design", "command", "query", "logging", "testmints", "kotlin")
        }
    }
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            gradlePlugin.testSourceSets(sources)
        }
    }
}

dependencies {
    implementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    implementation(libs.com.google.devtools.ksp)
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
        into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
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

    val projectResultPath = project.layout.buildDirectory.dir("test-output/${project.path}/results".replace(":", "/"))

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
                    email.set("rob@continuousexcellence.io")
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
