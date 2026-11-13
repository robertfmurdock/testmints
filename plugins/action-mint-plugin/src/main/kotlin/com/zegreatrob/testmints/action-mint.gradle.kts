package com.zegreatrob.testmints

plugins {
    base
    id("com.google.devtools.ksp")
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    configurations.matching { it.name.startsWith("ksp") && it.name != "ksp" }.all {
        dependencies.add(project.dependencies.create("com.zegreatrob.testmints:action-processor:${PluginVersions.bomVersion}"))
    }
    dependencies {
        "commonMainImplementation"("com.zegreatrob.testmints:action:${PluginVersions.bomVersion}")
        "commonMainImplementation"("com.zegreatrob.testmints:action-async:${PluginVersions.bomVersion}")
        "commonMainImplementation"("com.zegreatrob.testmints:action-annotation:${PluginVersions.bomVersion}")
    }
}
