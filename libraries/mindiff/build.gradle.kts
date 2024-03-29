plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        commonTest {
            dependencies {
                implementation(project(":standard"))
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        getByName("macosX64Main") { dependsOn(nativeCommonMain) }
        getByName("linuxX64Main") { dependsOn(nativeCommonMain) }
    }
}
