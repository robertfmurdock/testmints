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
    private val testFile by lazy { projectDir.resolve("src/test/kotlin/Test.kt") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test
    fun willConfigureKotlinJs() {
        settingsFile.writeText("""
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}")
            """.trimIndent())
        testFile.parentFile.mkdirs()
        testFile.writeBytes(
            this::class.java.getResourceAsStream("/Test.kt")!!.readAllBytes()
        )
        buildFile.writeText(
            """
            plugins {
                kotlin("js")
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
        runner.withPluginClasspath()
        runner.withArguments("test", "--info", "-P", "org.gradle.caching=true")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        val expected = """
Test.example STANDARD_OUT
    INFO: [testmints] {step=setup, state=start}
    setup
    [info] INFO: [testmints] {step=setup, state=finish}
    [info] INFO: [testmints] {step=exercise, state=start}
    exercise
    [info] INFO: [testmints] {step=exercise, state=finish}
    [info] INFO: [testmints] {step=verify, state=start, payload=kotlin.Unit}
    verify
    [info] INFO: [testmints] {step=verify, state=finish}
    """.trim()
        assertTrue(
            result.output.trim().contains(expected)
        )
    }

    @Test
    fun willConfigureKotlinJvm() {
        settingsFile.writeText("""
            rootProject.name = "testmints-functional-test"
            includeBuild("${System.getenv("ROOT_DIR")}")
            """.trimIndent())
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
        runner.withArguments("test", "--info", "-P", "org.gradle.caching=true")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        val expected = """
Test > example() STANDARD_ERROR
    [Test worker] INFO testmints - {step=test, state=start}
    [Test worker] INFO testmints - {step=setup, state=start}

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
        assertTrue(
            result.output.trim().contains(expected)
        )
    }
}
