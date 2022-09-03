plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.versioning")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    constraints {
        api("org.slf4j:slf4j-simple:2.0.0")
    }
}
