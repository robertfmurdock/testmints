plugins {
    base
}

repositories {
    mavenCentral()
}

val kotlinVersion = libs.versions.org.jetbrains.kotlin.get()
val coroutinesVersion = libs.versions.org.jetbrains.kotlinx.coroutines.get()
val serializationVersion = libs.versions.org.jetbrains.kotlinx.serialization.get()

allprojects {
    configurations.configureEach {
        if (isCanBeResolved) {
            resolutionStrategy.force(
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion",
                "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion",
                "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion",
                "org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion",
            )
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(kotlinVersion)
                }
                if (requested.group == "org.jetbrains.kotlinx") {
                    if (requested.name.startsWith("kotlinx-coroutines")) {
                        useVersion(coroutinesVersion)
                    }
                    if (requested.name.startsWith("kotlinx-serialization")) {
                        useVersion(serializationVersion)
                    }
                }
            }
        }
    }
}

tasks {
    register("versionCatalogUpdate") {}
    register("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
    }
    register("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
    }
    register("lintKotlin") {
        dependsOn(provider { (getTasksByName("lintKotlin", true) - this).toList() })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
    }
    clean {
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
}
