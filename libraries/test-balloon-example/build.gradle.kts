import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.testmints.plugins.platforms")
    alias(libs.plugins.de.infix.testBalloon)
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":async"))
                implementation(kotlin("test"))
                implementation("de.infix.testBalloon:testBalloon-framework-core")
            }
        }
    }
}

tasks {
    withType<FormatTask>().configureEach {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType<LintTask>().configureEach {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    wasmJsNodeTest {
        enabled = false
    }
    wasmJsTest {
        enabled = false
    }
    linuxX64Test {
        enabled = false
    }
}
