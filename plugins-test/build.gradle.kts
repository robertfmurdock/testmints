plugins {
    base
}

repositories {
    mavenCentral()
}

tasks {
    create("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
    }
    create("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
    }
    clean {
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
}
//
//afterEvaluate {
//    tasks {
//        "kotlinNpmInstall" {
//            dependsOn(gradle.includedBuild("libraries").task(":kotlinNpmInstall"))
//        }
//        "kotlinNodeJsSetup" {
//            dependsOn(provider { gradle.includedBuild("libraries").task(":kotlinNodeJsSetup") })
//        }
//    }
//}