plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {
    js {
        val compilation = compilations["test"]
        binaries.executable(compilation)

        compilation.packageJson {
            customField("mocha", mapOf("require" to "./kotlin/testmints-mint-logs.mjs"))
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
        commonMain {
            dependencies {
                implementation(project(":standard"))
                implementation(project(":report"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.coroutines.test)
                api(libs.kotlin.logging)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
}


dependencies {
    jvmTestImplementation(libs.slf4j.simple)
}
