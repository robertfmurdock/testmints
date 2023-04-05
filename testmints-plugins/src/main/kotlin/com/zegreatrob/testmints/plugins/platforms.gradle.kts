package com.zegreatrob.testmints.plugins

plugins {
    id("com.zegreatrob.testmints.plugins.multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm {}
        js(IR) { nodejs {} }
        macosX64()
        iosX64()
        linuxX64()
        mingwX64()
    }

    sourceSets {
        getByName("jvmTest") {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
            }
        }
    }
}

tasks {
    named("jvmTest", Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
    named("compileTestKotlinMingwX64") {
        mustRunAfter("signMingwX64Publication")
    }
    named("linkDebugTestMingwX64") {
        mustRunAfter("signMingwX64Publication")
    }
    named("compileTestKotlinLinuxX64") {
        mustRunAfter("signLinuxX64Publication")
    }
    named("linkDebugTestLinuxX64") {
        mustRunAfter("signLinuxX64Publication")
    }
}