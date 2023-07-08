package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.DefaultIncrementalSyncTask
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTestFramework
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma

plugins {
    base
}

afterEvaluate {

    val hooksConfiguration: Configuration by configurations.creating {
        isCanBeResolved = true
        isCanBeConsumed = false
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        attributes.attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KOTLIN_RUNTIME))
        attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
    }

    dependencies {
        hooksConfiguration("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
    }

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
        kotlinMultiplatform.js(configure = fun KotlinJsTargetDsl.() {
            val compilation = compilations["test"]

            (this as? KotlinJsIrTarget)?.let {
                it.whenBrowserConfigured { setupKarmaLogging(hooksConfiguration) }
                it.whenNodejsConfigured {
                    applyMochaSettings(compilation)
                }
            }

            tasks {
                named<DefaultIncrementalSyncTask>("jsTestTestDevelopmentExecutableCompileSync") {
                    from.from(zipTree(hooksConfiguration.resolve().first()))
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
    val newKarmaConfigDir = project.buildDir.resolve("karma.config.d")
    project.tasks {
        val karmaPrepare by registering(ProcessResources::class) {
            from(project.projectDir.resolve("karma.config.d"))
            from(
                project.zipTree(hooksConfiguration.resolve().first())
                    .filter { file -> file.name == "karma-mint-logs.js" }
            )
            into(newKarmaConfigDir)
        }
        testTask(Action<KotlinJsTest>(fun KotlinJsTest.() {
            dependsOn(karmaPrepare)
            onTestFrameworkSet(fun(framework: KotlinJsTestFramework?) {
                if (framework is KotlinKarma) {
                    framework.useConfigDirectory(newKarmaConfigDir)
                }
            })
        }))
    }
}

fun applyMochaSettings(compilation: KotlinJsCompilation) {
    val mochaSettings: Map<out Any?, Any?> = emptyMap<String, String>()

    val requires =
        mochaSettings["require"]?.let { if (it is String) listOf(it) else if (it is List<*>) it else null }
            ?: emptyList()
    compilation.packageJson {
        customField("mocha", mochaSettings + mapOf("require" to requires + "./kotlin/mint-logs.mjs"))
    }
}