package com.zegreatrob.testmints.plugins

import com.zegreatrob.testmints.plugins.BuildConstants.kotlinVersion
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
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }
}

ktlint {
    version.set("0.45.2")
}

repositories {
    mavenCentral()
}

dependencies {
    "commonMainApi"(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    "commonMainApi"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    "commonMainApi"(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.4.0"))
    "commonMainApi"(platform("org.junit:junit-bom:5.9.0"))
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
tasks.withType(org.jetbrains.kotlin.gradle.plugin.mpp.TransformKotlinGranularMetadata::class).configureEach {
    outputs.cacheIf { true }
}
