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
        js { nodejs {} }
        macosX64()
        iosX64()
        linuxX64()
        mingwX64()
    }

    sourceSets {
        getByName("jvmTest") {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
            }
        }
    }
}

tasks {
    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
}