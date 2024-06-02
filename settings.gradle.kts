pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "3.17.3"
}

rootProject.name = "testmints-root"

includeBuild("libraries")
includeBuild("plugins")
includeBuild("plugins-test")
includeBuild("convention-plugins")

val isCiServer = System.getenv("CI").isNotBlank()

develocity {
    buildScan {
        publishing.onlyIf { isCiServer }
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        tag("CI")
    }
}

buildCache {
    local { isEnabled = true }
}
