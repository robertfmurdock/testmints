plugins {
    base
}

tasks {
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