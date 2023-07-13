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

org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject)
project.rootProject.tasks.named("kotlinNpmInstall") {
    dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
}
project.rootProject.tasks.named("kotlinNodeJsSetup") {
    dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
}
