plugins {
    base
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

tasks {
    create("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
    }
    create("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
    }
    clean {
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
}