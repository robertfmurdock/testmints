rootProject.name = "convention-plugins"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal {
            content { includeGroup("org.jmailen.gradle") }
        }
    }
}
