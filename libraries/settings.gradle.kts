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

includeBuild("../convention-plugins")

include("action")
include("action-async")
include("async")
include("dependency-bom")
include("kotest-example")
include("minassert")
include("mindiff")
include("minspy")
include("mint-logs")
include("report")
include("standard")
include("testmints-bom")
