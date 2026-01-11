plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {

    sourceSets {
        commonMain {
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
    }
}
