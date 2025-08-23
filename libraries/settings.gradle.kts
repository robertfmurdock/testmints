dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "testmints"

includeBuild("../convention-plugins")

include("action")
include("action-annotation")
include("action-async")
include("action-processor")
include("async")
include("dependency-bom")
include("minassert")
include("mindiff")
include("minspy")
include("mint-logs")
include("report")
include("standard")
include("testmints-bom")
