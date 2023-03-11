package com.zegreatrob.testmints.logs

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages.KOTLIN_RUNTIME
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
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
        hooksConfiguration("com.zegreatrob.testmints:mint-logs")
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
            "testImplementation"("com.zegreatrob.testmints:mint-logs")
        }
    }

    val kotlinJs = extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension

    kotlinJs?.js {
        val compilation = compilations["test"]
        binaries.executable(compilation)

        compilation.packageJson {
            customField("mocha", mapOf("require" to "./kotlin/mint-logs.mjs"))
        }
        (this as? KotlinJsIrTarget)?.let {
            it.whenBrowserConfigured {
                val newKarmaConfigDir = buildDir.resolve("karma.config.d")

                tasks {
                    val karmaPrepare by registering(ProcessResources::class) {
                        from(projectDir.resolve("karma.conf.d"))
                        from(
                            zipTree(hooksConfiguration.resolve().first())
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


            tasks {
                val copySync = named("testTestProductionExecutableCompileSync", Copy::class) {
                    from(zipTree(hooksConfiguration.resolve().first()))
                }

                withType(KotlinJsTest::class) {
                    dependsOn(copySync)
                }
            }

            dependencies {
                "testImplementation"("com.zegreatrob.testmints:mint-logs")
            }
        }
    }

    val kotlinMultiplatform =
        extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

    kotlinMultiplatform?.targets?.findByName("js")?.let {
        kotlinMultiplatform.js {
            val compilation = compilations["test"]
            binaries.executable(compilation)

            compilation.packageJson {
                customField("mocha", mapOf("require" to "./kotlin/mint-logs.mjs"))
            }

            tasks {
                val copySync = named("jsTestTestProductionExecutableCompileSync", Copy::class) {
                    from(zipTree(hooksConfiguration.resolve().first()))
                }

                named("jsNodeTest") {
                    dependsOn(copySync)
                }
            }

            dependencies {
                "jsTestImplementation"("com.zegreatrob.testmints:mint-logs")
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
                "jvmTestImplementation"("com.zegreatrob.testmints:mint-logs")
            }
        }
    }
}

