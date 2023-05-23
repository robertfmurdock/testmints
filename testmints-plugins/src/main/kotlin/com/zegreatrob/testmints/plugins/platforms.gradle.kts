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
        tasks.findByPath("signMingwX64Publication")
            ?.let { mustRunAfter(it) }
    }
    named("linkDebugTestMingwX64") {
        tasks.findByPath("signMingwX64Publication")
            ?.let { mustRunAfter(it) }
    }
    named("compileTestKotlinLinuxX64") {
        tasks.findByPath("signLinuxX64Publication")
            ?.let { mustRunAfter(it) }
    }
    named("linkDebugTestLinuxX64") {
        tasks.findByPath("signLinuxX64Publication")
            ?.let { mustRunAfter(it) }
    }
}