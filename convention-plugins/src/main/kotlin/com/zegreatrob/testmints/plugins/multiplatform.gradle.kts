package com.zegreatrob.testmints.plugins

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
