package com.zegreatrob.testmints.plugins

plugins {
    kotlin("multiplatform")
    id("org.jmailen.kotlinter")
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

repositories {
    mavenCentral()
}

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))
}
