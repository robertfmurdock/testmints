plugins {
    id("com.zegreatrob.testmints.plugins.multiplatform")
    id("com.zegreatrob.testmints.plugins.publish")
    id("com.zegreatrob.testmints.plugins.platforms")
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }
        commonTest {
            dependencies {
                implementation(project(":standard"))
                implementation(project(":async"))
                implementation(project(":minassert"))
                implementation(project(":minspy"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        jvmMain {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
            }
        }

        jsMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

tasks {
    jvmTest {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
}
