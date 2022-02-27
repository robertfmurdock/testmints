package com.zegreatrob.testmints.plugins

plugins {
    kotlin("multiplatform")
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

dependencies {
    "commonMainImplementation"(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.0"))
}
