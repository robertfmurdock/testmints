plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.publish")
}

dependencies {
    constraints {
        api(project(":minassert"))
        api(project(":standard"))
        api(project(":async"))
        api(project(":action"))
        api(project(":action-async"))
        api(project(":minspy"))
        api(project(":mindiff"))
        api(project(":report"))
        api(project(":mint-logs"))
        api(project(":mint-logs-plugin"))
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
