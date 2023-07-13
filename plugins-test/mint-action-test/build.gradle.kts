import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion

plugins {
    id("com.zegreatrob.testmints.action-mint")
    id("com.zegreatrob.testmints.plugins.multiplatform")
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.platforms")
}

tasks {

}

org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject)
project.rootProject.tasks.named("kotlinNpmInstall") {
    dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
}
project.rootProject.tasks.named("kotlinNodeJsSetup") {
    dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
}
