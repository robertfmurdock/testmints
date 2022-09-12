plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":standard"))
                api(project(":report"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        val iosX64Main by getting { dependsOn(nativeCommonMain) }

        val mingwX64Main by getting { dependsOn(nativeCommonMain) }

        val jsMain by getting {
            dependencies {
                dependsOn(commonMain)
            }
        }
    }
}

tasks {

    // Appears to be a bug with delay on these platforms with the new compiler. We'll bring these back online later.

    named("linuxX64Test") {
        enabled = false
    }
    named("macosX64Test") {
        enabled = false
    }
    named("iosX64Test") {
        enabled = false
    }
}
