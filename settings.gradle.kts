pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    `gradle-enterprise`
}

rootProject.name = "testmints"

dependencyResolutionManagement {
    versionCatalogs(fun MutableVersionCatalogContainer.() {
        create("libs") {
            from(files("testmints-libraries/gradle/libs.versions.toml"))
        }
    }
    )
}

includeBuild("testmints-libraries")
includeBuild("testmints-plugins")
includeBuild("testmints-convention-plugins")

val isCiServer = System.getenv().containsKey("CI")

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}

buildCache {
    local { isEnabled = true }
}
