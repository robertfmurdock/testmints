dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("action-mint-plugin")
include("mint-logs-plugin")

includeBuild("../convention-plugins")
