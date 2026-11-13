plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        commonTest {
            dependencies {
                implementation(project(":standard"))
                implementation(kotlin("test"))
            }
        }
    }
}
