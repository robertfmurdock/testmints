plugins {
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
        commonMain {
            dependencies {
                api(project(":standard"))
                api(project(":report"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
                api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
            }
        }
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
            }
        }
    }
}
