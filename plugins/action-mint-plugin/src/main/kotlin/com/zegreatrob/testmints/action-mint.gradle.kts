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
            "commonMainImplementation"("com.zegreatrob.testmints:action")
            "commonMainImplementation"("com.zegreatrob.testmints:action-async")

            configurations.names.forEach {
                if(it.startsWith("ksp")){
                    it("com.zegreatrob.testmints:action-processor")
                }
            }
        }
    }
}