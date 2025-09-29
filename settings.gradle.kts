plugins {
    id("com.gradle.develocity") version "3.17.3"
}

rootProject.name = "testmints-root"

includeBuild("libraries")
includeBuild("plugins")
includeBuild("plugins-test")
includeBuild("convention-plugins")

develocity {
    buildScan {
        publishing.onlyIf { System.getenv("CI").isNullOrBlank().not() }
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        tag("CI")
    }
}

buildCache {
    local { isEnabled = true }
}
