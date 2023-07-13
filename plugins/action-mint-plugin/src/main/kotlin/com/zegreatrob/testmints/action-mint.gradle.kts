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
                if (
                    it.startsWith("ksp") && it != "ksp"
                    && !it.contains("common", true)
                ) {
                    it("com.zegreatrob.testmints:action-processor")
                }
            }
            "commonMainImplementation"("com.zegreatrob.testmints:action")
            "commonMainImplementation"("com.zegreatrob.testmints:action-async")
            "commonMainImplementation"("com.zegreatrob.testmints:action-annotation")
        }
    }
}
