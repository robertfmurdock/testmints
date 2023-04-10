package tagger

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
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
            includeBuild("${System.getenv("ROOT_DIR")}")
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
                kotlin("js") version "1.8.20"
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
                implementation(kotlin("test"))
                implementation("com.zegreatrob.testmints:standard")
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
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
            result.output.trim().contains(nodeJsExpectedOutput)
        )
    }

    @Test
    fun willConfigureKotlinJsBrowser() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}")
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
                kotlin("js") version "1.8.20"
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
                implementation(kotlin("test"))
                implementation("com.zegreatrob.testmints:standard")
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
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
            result.output.trim().contains(browserJsExpectedOutput)
        )
    }

    @Test
    fun willConfigureMultiplatform() {
        settingsFile.writeText(
            """
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}")
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
                kotlin("multiplatform") version "1.8.20"
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            
            repositories {
                mavenCentral()
            }
            
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
            result.output.trim().contains(nodeJsExpectedOutput),
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
            includeBuild("${System.getenv("ROOT_DIR")}")
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
                kotlin("jvm")
                id("com.zegreatrob.testmints.logs.mint-logs")
            }
            repositories {
                mavenCentral()
            }
            tasks {
                named("test", Test::class) {
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
        )
    }

    private val jvmExpectedOutput = """
Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - {step=test, state=start}
    [Test worker] INFO testmints - {step=setup, state=start, name=Test.example()}

Test > example() STANDARD_OUT
    setup

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - {step=setup, state=finish}
    [Test worker] INFO testmints - {step=exercise, state=start}

Test > example() STANDARD_OUT
    exercise

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - {step=exercise, state=finish}
    [Test worker] INFO testmints - {step=verify, state=start, payload=kotlin.Unit}

Test > example() STANDARD_OUT
    verify

Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - {step=verify, state=finish}
    [Test worker] INFO testmints - {step=test, state=finish}
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
Test.example STANDARD_OUT
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

    private val browserJsExpectedOutput = """
Test.example STANDARD_OUT
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
