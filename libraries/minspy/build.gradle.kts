plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

dependencies {
    commonMainImplementation(kotlin("test"))
    commonTestImplementation(project(":standard"))
    commonTestImplementation(project(":minassert"))
    commonTestImplementation(kotlin("test"))
}
