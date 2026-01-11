plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

dependencies {
    commonMainImplementation(project(":mindiff"))
    commonMainImplementation(kotlin("test"))

    commonTestImplementation(project(":standard"))
    commonTestImplementation(kotlin("test"))
}
