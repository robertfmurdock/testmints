package com.zegreatrob.testmints

plugins {
    base
    id("com.google.devtools.ksp")
}

afterEvaluate {
    val kotlinMultiplatform =
        extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


    kotlinMultiplatform?.run {
        dependencies {
            configurations.names.forEach {
                if (it.startsWith("ksp") && it != "ksp") {
                    it("com.zegreatrob.testmints:action-processor:${PluginVersions.bomVersion}")
                }
            }
            "commonMainImplementation"("com.zegreatrob.testmints:action:${PluginVersions.bomVersion}")
            "commonMainImplementation"("com.zegreatrob.testmints:action-async:${PluginVersions.bomVersion}")
            "commonMainImplementation"("com.zegreatrob.testmints:action-annotation:${PluginVersions.bomVersion}")
        }
    }
}
