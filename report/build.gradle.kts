plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }
        val nativeCommonMain by creating { dependsOn(commonMain) }
        getByName("macosX64Main") { dependsOn(nativeCommonMain) }
        getByName("iosX64Main") { dependsOn(nativeCommonMain) }
        getByName("linuxX64Main") { dependsOn(nativeCommonMain) }
        getByName("mingwX64Main") { dependsOn(nativeCommonMain) }
        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}
