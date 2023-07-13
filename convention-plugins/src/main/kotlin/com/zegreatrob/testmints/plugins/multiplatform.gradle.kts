package com.zegreatrob.testmints.plugins

import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyTransformationTask
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmCachesSetup
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.testmints.plugins.reports")
    id("org.jmailen.kotlinter")
}

kotlin {
    jvmToolchain(11)
    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    "commonMainApi"(platform(
        findProject(":dependency-bom") ?: "com.zegreatrob.testmints:dependency-bom"
    ))
}

tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.PublicPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNativeSimulatorTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNpmCachesSetup::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNativeHostTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinJsIrLink::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(Kotlin2JsCompile::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNativeCompile::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinNativeLink::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.jvm.tasks.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinJvmTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(MetadataDependencyTransformationTask::class).configureEach {
    outputs.cacheIf { true }
}
