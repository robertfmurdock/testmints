plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(21)
    jvm()
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(libs.kotlinpoet.ksp)
                implementation(libs.ksp.symbol.processing.api)
            }
        }
    }
}
