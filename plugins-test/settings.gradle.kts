dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libraries/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../libraries")
includeBuild("../plugins")
includeBuild("../convention-plugins")

include("mint-action-test")
