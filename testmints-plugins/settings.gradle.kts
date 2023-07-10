dependencyResolutionManagement {
    versionCatalogs(fun MutableVersionCatalogContainer.() {
        create("libs") {
            from(files("../testmints-libraries/gradle/libs.versions.toml"))
        }
    }
    )
}

include("mint-logs-plugin")