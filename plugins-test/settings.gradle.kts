pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    `gradle-enterprise`
}

includeBuild("../libraries")
includeBuild("../plugins")
includeBuild("../convention-plugins")

include("mint-action-test")