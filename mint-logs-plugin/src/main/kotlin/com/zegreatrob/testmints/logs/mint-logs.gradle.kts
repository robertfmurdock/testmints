package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute

plugins {
    base
}

afterEvaluate {

    val kotlinJs = extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension

    kotlinJs?.js {
        val compilation = compilations["test"]
        binaries.executable(compilation)

        compilation.packageJson {
            customField("mocha", mapOf("require" to "./kotlin/mint-logs.mjs"))
        }
    }

    val hooksConfiguration: Configuration by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        attributes.attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KOTLIN_RUNTIME))
        attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
    }

    dependencies {
        "testImplementation"("com.zegreatrob.testmints:mint-logs")
        hooksConfiguration("com.zegreatrob.testmints:mint-logs") {
            attributes {

            }
        }
    }

    tasks {
        val copySync = named("testTestProductionExecutableCompileSync", Copy::class) {
            from(
                zipTree(
                    hooksConfiguration.resolve()
                        .first()
                )
            )
        }

        val debugMe by registering {
            dependsOn("testTestProductionExecutableCompileSync")
            doLast {
                copySync.get().destinationDir.listFiles()
                    .joinToString("\n") { it.absolutePath }
                    .also { println("compileDirectoryContents=[$it]") }
            }
        }


        named("nodeTest") {
            dependsOn("testTestProductionExecutableCompileSync", debugMe)

        }

    }

}

