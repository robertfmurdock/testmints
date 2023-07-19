plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.serialization.bom))
    api(platform(libs.org.junit.junit.bom))
    constraints {
        api(libs.ch.qos.logback.contrib.logback.jackson)
        api(libs.ch.qos.logback.contrib.logback.json.classic)
        api(libs.ch.qos.logback.logback.classic)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
        api(libs.io.github.oshai.kotlin.logging)
        api(libs.io.github.oshai.kotlin.logging.jvm)
        api(libs.net.logstash.logback.logstash.logback.encoder)
        api(libs.com.fasterxml.jackson.dataformat.jackson.dataformat.yml)
        api(libs.org.apache.logging.log4j.log4j.api)
        api(libs.org.apache.logging.log4j.log4j.core)
        api(libs.org.apache.logging.log4j.log4j.iostreams)
        api(libs.org.apache.logging.log4j.log4j.slf4j2.impl)
        api(libs.org.slf4j.slf4j.api)
        api(libs.org.slf4j.slf4j.simple)
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
