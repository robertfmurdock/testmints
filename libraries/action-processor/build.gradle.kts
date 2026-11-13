plugins {
    id("com.zegreatrob.testmints.plugins.publish")
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(libs.com.squareup.kotlinpoet.ksp)
                implementation(libs.com.google.devtools.ksp.symbol.processing.api)
            }
        }
    }
}
