package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.PublicPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
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
            named("test", Test::class) {
                useJUnitPlatform()
                systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
            }
        }
        dependencies {
            "testImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
        }
    }

    val kotlinJs = extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension

    kotlinJs?.js {
        (this as? KotlinJsIrTarget)?.let {
            val compilation = compilations["test"]
            binaries.executable(compilation)

            it.whenBrowserConfigured { setupKarmaLogging(hooksConfiguration) }

            tasks {
                named("publicPackageJson", PublicPackageJsonTask::class) {
                    val mochaSettings = packageJsonCustomFields.getOrDefault("mocha", null) as? Map<*, *>
                        ?: emptyMap<String, String>()
                    compilation.packageJson {
                        customField("mocha", mochaSettings + mapOf("require" to "./kotlin/mint-logs.mjs"))
                    }
                }

                val copySync = named("testTestProductionExecutableCompileSync", Copy::class) {
                    from(zipTree(hooksConfiguration.resolve().first()))
                }

                withType(KotlinJsTest::class) {
                    dependsOn(copySync)
                }
            }

            dependencies {
                "testImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        }
    }

    val kotlinMultiplatform =
        extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    kotlinMultiplatform?.targets?.findByName("js")?.let {
        kotlinMultiplatform.js {
            val compilation = compilations["test"]
            binaries.executable(compilation)

            (this as? KotlinJsIrTarget)?.let {
                it.whenBrowserConfigured { setupKarmaLogging(hooksConfiguration) }
                it.whenNodejsConfigured {
                    tasks {
                        named("jsPublicPackageJson", PublicPackageJsonTask::class) {
                            val mochaSettings = packageJsonCustomFields.getOrDefault("mocha", null) as? Map<*, *>
                                ?: emptyMap<String, String>()
                            compilation.packageJson {
                                customField("mocha", mochaSettings + mapOf("require" to "./kotlin/mint-logs.mjs"))
                            }
                        }
                    }
                }
            }

            tasks {
                val copySync = named("jsTestTestProductionExecutableCompileSync", Copy::class) {
                    from(zipTree(hooksConfiguration.resolve().first()))
                }
                withType(KotlinJsTest::class) {
                    dependsOn(copySync)
                }
            }

            dependencies {
                "jsTestImplementation"("com.zegreatrob.testmints:mint-logs:${PluginVersions.bomVersion}")
            }
        }
    }

    kotlinMultiplatform?.targets?.findByName("jvm")?.let {
        kotlinMultiplatform.jvm {
            tasks {
                named("jvmTest", Test::class) {
                    useJUnitPlatform()
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
            from(project.projectDir.resolve("karma.conf.d"))
            from(
                project.zipTree(hooksConfiguration.resolve().first())
                    .filter { file -> file.name == "karma-mint-logs.js" }
            )
            into(newKarmaConfigDir)
        }
        testTask {
            dependsOn(karmaPrepare)
            onTestFrameworkSet { framework ->
                if (framework is KotlinKarma) {
                    framework.useConfigDirectory(newKarmaConfigDir)
                }
            }
        }
    }
}
