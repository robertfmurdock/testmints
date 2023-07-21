plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {
    sourceSets {
        val commonMain by getting
        val nativeCommonMain by creating { dependsOn(commonMain) }
        getByName("macosX64Main") { dependsOn(nativeCommonMain) }
        getByName("linuxX64Main") { dependsOn(nativeCommonMain) }
    }
}

dependencies {
    commonMainImplementation(project(":mindiff"))
    commonMainImplementation(kotlin("test"))

    commonTestImplementation(project(":standard"))
    commonTestImplementation(kotlin("test"))
}
