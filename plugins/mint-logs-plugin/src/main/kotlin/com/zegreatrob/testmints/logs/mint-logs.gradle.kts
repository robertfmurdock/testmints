package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonArray
import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.JsonObject
import org.jetbrains.kotlin.com.google.gson.JsonPrimitive
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.DefaultIncrementalSyncTask
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.PackageJsonTypeAdapter
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTestFramework
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma
import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty

plugins {
    base
}

afterEvaluate {

    val kotlinJvm = extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

    kotlinJvm?.apply {
        tasks {
            named<Test>("test") {
                systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
            }
        }
        dependencies {
            "testImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
        }
    }

    val kotlinMultiplatform =
        extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    if (kotlinMultiplatform?.targets?.findByName("js") != null) {


        val hooksConfiguration: Configuration by configurations.creating {
            isCanBeResolved = true
            isCanBeConsumed = false

            usesPlatformOf(kotlinMultiplatform.js().compilations.getByName("test").target)
        }

        dependencies {
            hooksConfiguration("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
        }

        kotlinMultiplatform.js(configure = fun KotlinJsTargetDsl.() {
            val compilation = compilations["test"]

            (this as? KotlinJsIrTarget)?.let {
                it.whenBrowserConfigured { setupKarmaLogging(hooksConfiguration) }
                it.whenNodejsConfigured {
                    val target = compilation.compileTaskProvider.get().compilerOptions.target.get()
                    applyMochaSettings(compilation, target)
                }
            }

            tasks {
                named<DefaultIncrementalSyncTask>("jsTestTestDevelopmentExecutableCompileSync") {
                    dependsOn(hooksConfiguration)
                    from.from(zipTree(hooksConfiguration.first()))
                }
            }

            dependencies {
                "jsTestImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        })
    }

    kotlinMultiplatform?.targets?.findByName("jvm")?.let {
        kotlinMultiplatform.jvm {
            tasks {
                named<Test>("jvmTest") {
                    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
                }
            }
            dependencies {
                "jvmTestImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        }
    }
}

fun KotlinJsBrowserDsl.setupKarmaLogging(hooksConfiguration: Configuration) {
    val newKarmaConfigDir = project.layout.buildDirectory.dir("karma.config.d")
    project.tasks {
        val karmaPrepare by registering(ProcessResources::class) {
            dependsOn(hooksConfiguration)
            from(project.projectDir.resolve("karma.config.d"))
            from(
                project.zipTree(hooksConfiguration.first())
                    .filter { file -> file.name == "karma-mint-logs.js" }
            )
            into(newKarmaConfigDir)
        }
        testTask(Action<KotlinJsTest>(fun KotlinJsTest.() {
            dependsOn(karmaPrepare)
            onTestFrameworkSet(fun(framework: KotlinJsTestFramework?) {
                if (framework is KotlinKarma) {
                    framework.useConfigDirectory(newKarmaConfigDir.get().asFile)
                }
            })
        }))
    }
}

fun applyMochaSettings(compilation: KotlinJsCompilation, target: String) {
    compilation.packageJson {
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapterFactory(PackageJsonTypeAdapter())
            .create()
        val jsonTree = gson.toJsonTree(this@packageJson)
        val mochaSettings = jsonTree.asJsonObject?.get("mocha")?.asJsonObject ?: JsonObject()

        val jsonElement: JsonElement? = mochaSettings["require"]
        val previousRequires: List<JsonElement> = jsonElement?.let {
            if (it.isJsonPrimitive) {
                listOf(it)
            } else if (it.isJsonArray) {
                it.toSetOrEmpty().toList()
            } else {
                null
            }
        }
            ?: emptyList()
        val suffix = if (target == "es5") "js" else "mjs"
        val requires = previousRequires + JsonPrimitive("./kotlin/testmints-mint-logs.$suffix")
        mochaSettings.add("require", JsonArray().apply { requires.forEach(::add) })
        customField("mocha", gson.toJson(mochaSettings).let { gson.fromJson(it, Map::class.java) })
    }
}
