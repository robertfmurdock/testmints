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
        settingsFile.writeText("""includeBuild("${System.getenv("ROOT_DIR")}")""")
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
    setup
    exercise
    verify"""
        assertTrue(
            result.output.trim().contains(expected)
        )
    }
}
