import com.zegreatrob.testmints.plugins.BuildConstants.kotlinVersion

plugins {
    id("com.zegreatrob.testmints.plugins.multiplatform")
    id("com.zegreatrob.testmints.plugins.versioning")
    id("com.zegreatrob.testmints.plugins.publish")
}

kotlin {

    targets {
        js { nodejs() }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":action"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":standard"))
                implementation(project(":async"))
                implementation(project(":minassert"))
                implementation(project(":minspy"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        getByName("jvmMain") {
            dependencies {
                implementation(kotlin("reflect", kotlinVersion))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.slf4j:slf4j-simple")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
    }
}

tasks {
    named<Test>("jvmTest") {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }
}
