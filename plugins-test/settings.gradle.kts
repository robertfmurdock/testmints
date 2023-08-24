pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("../libraries")
includeBuild("../plugins")
includeBuild("../convention-plugins")

include("mint-action-test")
