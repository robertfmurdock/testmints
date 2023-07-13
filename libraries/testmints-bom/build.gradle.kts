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
        api(project(":action-annotation"))
        api(project(":action-processor"))
        api(project(":minspy"))
        api(project(":mindiff"))
        api(project(":report"))
        api(project(":mint-logs"))
        api("com.zegreatrob.testmints:mint-logs-plugin:${rootProject.version}")
        api("com.zegreatrob.testmints:action-mint-plugin:${rootProject.version}")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
