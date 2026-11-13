rootProject.name = "convention-plugins"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal {
            content {
                includeGroup("org.jmailen.gradle")
                includeModule("io.github.gradle-nexus", "publish-plugin")
            }
        }
    }
}
