import org.gradle.kotlin.dsl.withType
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
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
    withType(FormatTask::class) {
        dependsOn("generateTestBalloonInitialization")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        dependsOn("generateTestBalloonInitialization")
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    wasmJsNodeTest {
        enabled = false
    }
    wasmJsTest {
        enabled = false
    }
}
