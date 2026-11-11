pluginManagement {
    includeBuild("../../ze-great-tools/tools-plugins")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("action-mint-plugin")
include("mint-logs-plugin")
