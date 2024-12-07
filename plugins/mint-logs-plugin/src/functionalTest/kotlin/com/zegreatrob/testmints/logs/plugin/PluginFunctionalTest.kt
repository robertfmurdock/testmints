package com.zegreatrob.testmints.logs.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    private val releaseVersion = System.getenv("RELEASE_VERSION")

    @Test
    fun willConfigureKotlinJsNode() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}/../libraries")
            """.trimIndent()
        )
        val testFile = projectDir.resolve("src/commonTest/kotlin/Test.kt")
        testFile.parentFile.mkdirs()
        testFile.writeBytes(
            this::class.java.getResourceAsStream("/Test.kt")!!.readAllBytes()
        )
        buildFile.writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.1.0"
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            
            repositories {
                mavenCentral()
            }
            
            kotlin {
                js(IR) {
                    nodejs()
                }
            }
            dependencies {
                "jsMainImplementation"(kotlin("test"))
                "jsMainImplementation"("com.zegreatrob.testmints:standard")
            }
            
            
            rootProject.extensions.findByType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension::class.java).let {
                if (it?.version != "22.5.1") {
                    it?.version = "22.5.1"
                }
            }
            
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "jsTest",
            "--info",
            "-P",
            "org.gradle.caching=true",
            "-Pversion=$releaseVersion"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()
        assertTrue(
            result.output.trim().contains(multiplatformNodeJsExpectedOutput)
        )
    }

    @Test
    @Disabled("temporary until can figure out what changed about the karma mocha setup")
    fun willConfigureKotlinJsBrowser() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}/../libraries")
            """.trimIndent()
        )
        val testFile = projectDir.resolve("src/commonTest/kotlin/Test.kt")
        testFile.parentFile.mkdirs()
        testFile.writeBytes(
            this::class.java.getResourceAsStream("/Test.kt")!!.readAllBytes()
        )
        buildFile.writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.1.0"
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            
            repositories {
                mavenCentral()
            }
            
            kotlin {
                js(IR) {
                    browser()
                }
            }
            dependencies {
                "jsMainImplementation"(kotlin("test"))
                "jsMainImplementation"("com.zegreatrob.testmints:standard")
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "jsTest",
            "--info",
            "-P",
            "org.gradle.caching=true",
            "-Pversion=$releaseVersion"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()
        assertTrue(
            result.output.trim().contains(browserJsExpectedOutput)
        )
    }

    @Test
    @Disabled("temporary until can figure out what changed about the karma mocha setup")
    fun willConfigureMultiplatform() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}/../libraries")
            """.trimIndent()
        )
        val testFile = projectDir.resolve("src/commonTest/kotlin/Test.kt")
        testFile.parentFile.mkdirs()
        testFile.writeBytes(
            this::class.java.getResourceAsStream("/Test.kt")!!.readAllBytes()
        )
        buildFile.writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.1.0"
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            
            repositories {
                mavenCentral()
            }º
            
            kotlin {
                js(IR) {
                    nodejs()
                    browser()
                }
                jvm()
            }
            
            tasks {
                named("jvmTest", Test::class) {
                    useJUnitPlatform()
                }
            }
            
            dependencies {
                "commonTestImplementation"(kotlin("test"))
                "commonTestImplementation"("com.zegreatrob.testmints:standard")
                "jvmTestImplementation"("org.slf4j:slf4j-simple:2.0.6")
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.withPluginClasspath()
        runner.forwardOutput()
        runner.withArguments(
            "jsTest",
            "jvmTest",
            "--info",
            "-P",
            "org.gradle.caching=true",
            "-Pversion=$releaseVersion"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertTrue(
            result.output.trim().contains(multiplatformNodeJsExpectedOutput),
            "Node Js did not have expected output"
        )
        assertTrue(
            result.output.trim().contains(browserJsExpectedOutput),
            "Browser Js did not have expected output"
        )
        assertTrue(
            result.output.trim().contains(multiplatformJvmExpectedOutput),
            "Jvm did not have expected output"
        )
    }

    @Test
    fun willConfigureKotlinJvm() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}/../libraries")
            """.trimIndent()
        )
        val testFile = projectDir.resolve("src/test/kotlin/Test.kt")
        testFile.parentFile.mkdirs()
        testFile.writeBytes(
            this::class.java.getResourceAsStream("/Test.kt")!!.readAllBytes()
        )
        buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "2.1.0"
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            repositories {
                mavenCentral()
            }
            tasks {
                named("test", Test::class) {
                    systemProperty("slf4j.provider", "org.slf4j.simple.SimpleServiceProvider")
                    systemProperty("slf4j.internal.verbosity", "WARN")
                    useJUnitPlatform()
                }
            }
            dependencies {
                implementation(kotlin("test"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("org.slf4j:slf4j-simple:2.0.6")
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "test",
            "--info",
            "-P",
            "org.gradle.caching=true",
            "-Pversion=$releaseVersion"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertTrue(
            result.output.trim().contains(jvmExpectedOutput)
        ) { "${result.output}\n did not contain \n$jvmExpectedOutput" }
    }

    private val jvmExpectedOutput = """
Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - step=test state=start test-start
    [Test worker] INFO testmints - step=setup state=start name=Test.example() setup-start

Test > example() STANDARD_OUT
    setup

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - step=setup state=finish setup-finish
    [Test worker] INFO testmints - step=exercise state=start exercise-start

Test > example() STANDARD_OUT
    exercise

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - step=exercise state=finish exercise-finish
    [Test worker] INFO testmints - step=verify state=start payload=kotlin.Unit verify-start

Test > example() STANDARD_OUT
    verify

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - step=verify state=finish verify-finish
    [Test worker] INFO testmints - step=test state=finish test-finish
    """.trim()

    private val multiplatformJvmExpectedOutput = """
Test[jvm] > example()[jvm] STANDARD_ERROR
    [Test worker] INFO testmints - {step=test, state=start}
    [Test worker] INFO testmints - {step=setup, state=start, name=Test.example()}

Test[jvm] > example()[jvm] STANDARD_OUT
    setup

Test[jvm] > example()[jvm] STANDARD_ERROR
    [Test worker] INFO testmints - {step=setup, state=finish}
    [Test worker] INFO testmints - {step=exercise, state=start}

Test[jvm] > example()[jvm] STANDARD_OUT
    exercise

Test[jvm] > example()[jvm] STANDARD_ERROR
    [Test worker] INFO testmints - {step=exercise, state=finish}
    [Test worker] INFO testmints - {step=verify, state=start, payload=kotlin.Unit}

Test[jvm] > example()[jvm] STANDARD_OUT
    verify

Test[jvm] > example()[jvm] STANDARD_ERROR
    [Test worker] INFO testmints - {step=verify, state=finish}
    [Test worker] INFO testmints - {step=test, state=finish}
    """.trim()

    private val nodeJsExpectedOutput = """
Test.example[node] STANDARD_OUT
    INFO: [testmints] {step=setup, state=start, name=Test.example}
    setup
    [info] INFO: [testmints] {step=setup, state=finish}
    [info] INFO: [testmints] {step=exercise, state=start}
    exercise
    [info] INFO: [testmints] {step=exercise, state=finish}
    [info] INFO: [testmints] {step=verify, state=start, payload=kotlin.Unit}
    verify
    [info] INFO: [testmints] {step=verify, state=finish}
    """.trim()

    private val multiplatformNodeJsExpectedOutput = """
Test.example[js, node] STANDARD_OUT
    INFO: [testmints] setup-start {step=setup, state=start, name=Test.example}
    setup
    [info] INFO: [testmints] setup-finish {step=setup, state=finish}
    [info] INFO: [testmints] exercise-start {step=exercise, state=start}
    exercise
    [info] INFO: [testmints] exercise-finish {step=exercise, state=finish}
    [info] INFO: [testmints] verify-start {step=verify, state=start, payload=kotlin.Unit}
    verify
    [info] INFO: [testmints] verify-finish {step=verify, state=finish}
    """.trim()

    private val browserJsExpectedOutput = """
    INFO: [testmints] {step=setup, state=start, name=Test.example}
    [log] setup
    [info] INFO: [testmints] {step=setup, state=finish}
    [info] INFO: [testmints] {step=exercise, state=start}
    [log] exercise
    [info] INFO: [testmints] {step=exercise, state=finish}
    [info] INFO: [testmints] {step=verify, state=start, payload=kotlin.Unit}
    [log] verify
    [info] INFO: [testmints] {step=verify, state=finish}
    """.trim()
}
