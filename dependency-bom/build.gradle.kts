plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom:${com.zegreatrob.testmints.plugins.BuildConstants.kotlinVersion}"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    api(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.4.0"))
    api(platform("org.junit:junit-bom:5.9.0"))
    constraints {
        api("org.slf4j:slf4j-simple:2.0.0")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
