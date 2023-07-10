dependencyResolutionManagement {
    versionCatalogs(fun MutableVersionCatalogContainer.() {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    )
}

include("mint-logs-plugin")