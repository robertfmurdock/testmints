import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.testmints.action-mint")
    id("com.zegreatrob.testmints.plugins.multiplatform")
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.platforms")
}

dependencies {
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")
    "commonTestImplementation"("com.zegreatrob.testmints:minspy")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"(kotlin("test"))
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = false
    }
}

org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject)
project.rootProject.tasks.named("kotlinNpmInstall") {
    dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
}
project.rootProject.tasks.named("kotlinNodeJsSetup") {
    dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
}

tasks {
    formatKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    formatKotlinJsTest {
        dependsOn("kspTestKotlinJs")
    }
    withType(FormatTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    lintKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinJsTest {
        dependsOn("kspTestKotlinJs")
    }
}
