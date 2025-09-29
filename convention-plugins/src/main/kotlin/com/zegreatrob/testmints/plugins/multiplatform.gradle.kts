package com.zegreatrob.testmints.plugins

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.testmints.plugins.reports")
    id("org.jmailen.kotlinter")
}

kotlin {
    jvmToolchain(17)
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        allWarningsAsErrors = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    "commonMainApi"(
        platform(
            findProject(":dependency-bom") ?: "com.zegreatrob.testmints:dependency-bom"
        )
    )
}
