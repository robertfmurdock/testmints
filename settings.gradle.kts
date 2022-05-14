plugins {
    `gradle-enterprise`
}

rootProject.name = "testmints"

include("minassert")
include("standard")
include("kotest-example")
include("async")
include("action")
include("action-async")
include("minspy")
include("mindiff")
include("report")
include("testmints-bom")

includeBuild("testmints-plugins")

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
