package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonArray
import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.JsonObject
import org.jetbrains.kotlin.com.google.gson.JsonPrimitive
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
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

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.named<Test>("test") {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    }
    dependencies {
        "testImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
    kotlin.targets.withType<KotlinJsIrTarget>().matching { it.name == "js" }.all {
        val jsTarget = this
        val hooksConfiguration: Configuration = configurations.create("hooks") {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false
//            @Suppress("OPT_IN_USAGE")
//            attributes.attribute(KlibPackaging.ATTRIBUTE, project.objects.named(KlibPackaging.PACKED))
            attributes.attribute(
                Usage.USAGE_ATTRIBUTE,
                project.objects.named<Usage>(Usage::class.java, "kotlin-runtime")
            )
            usesPlatformOf(jsTarget)
        }

        dependencies {
            hooksConfiguration("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
        }

        val extractMintLogsHooks = tasks.register<Sync>("extractMintLogsHooks") {
            from(zipTree(hooksConfiguration.elements.map { it.single().asFile }))
            into(layout.buildDirectory.dir("mint-logs/hooks"))
        }
        val extractedHooks = extractMintLogsHooks.map { it.destinationDir }

        compilations.named("test") {
            val compilation = this
            jsTarget.whenBrowserConfigured { setupKarmaLogging(extractedHooks) }
            jsTarget.whenNodejsConfigured {
                val target = compilation.compileTaskProvider.flatMap { it.compilerOptions.target }
                applyMochaSettings(compilation, target)
            }
            defaultSourceSet.dependencies {
                implementation("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        }

        tasks.withType<DefaultIncrementalSyncTask>().configureEach {
            if (name == "jsTestTestDevelopmentExecutableCompileSync") {
                from.from(extractedHooks)
            }
        }
    }

    kotlin.targets.matching { it.name == "jvm" }.all {
        compilations.named("test") {
            defaultSourceSet.dependencies {
                implementation("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        }
        tasks.withType<Test>().configureEach {
            if (name == "jvmTest") {
                systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
            }
        }
    }
}

fun KotlinJsBrowserDsl.setupKarmaLogging(extractedHooks: Provider<File>) {
    val newKarmaConfigDir = project.layout.buildDirectory.dir("karma.config.d")
    project.tasks {
        val karmaPrepare = register("karmaPrepare", ProcessResources::class) {
            from(project.projectDir.resolve("karma.config.d"))
            from(extractedHooks) {
                include("**/karma-mint-logs.js")
            }
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

fun applyMochaSettings(compilation: KotlinJsCompilation, target: Provider<String>) {
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
        val suffix = if (target.get() == "es5") "js" else "mjs"
        val requires = previousRequires + JsonPrimitive("./kotlin/testmints-mint-logs.$suffix")
        mochaSettings.add("require", JsonArray().apply { requires.forEach(::add) })
        customField("mocha", gson.toJson(mochaSettings).let { gson.fromJson(it, Map::class.java) })
    }
}
