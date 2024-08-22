plugins {
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.platforms")
    alias(libs.plugins.io.kotest.multiplatform)
}

kotlin {

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":standard"))
                implementation(kotlin("stdlib"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.io.kotest.kotest.framework.engine)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.io.kotest.kotest.runner.junit5.jvm)
            }
        }
    }
}

tasks {
    wasmJsNodeTest {
        enabled = false
    }
}