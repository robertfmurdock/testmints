plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.com.gradle.plugin.publish)
    base
    id("org.jetbrains.kotlin.jvm") version(embeddedKotlinVersion)
    id("com.zegreatrob.testmints.plugins.published-gradle-plugin")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors = true
    }
}

gradlePlugin {
    plugins {
        named("com.zegreatrob.testmints.logs.mint-logs") {
            displayName = "Testmints Mint-Logs Plugin"
            description = "This plugin will enabling stage logging for testmints tests."
            tags.addAll("testing", "logging", "testmints", "kotlin")
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$embeddedKotlinVersion"))
    implementation(kotlin("gradle-plugin", embeddedKotlinVersion))
    implementation(kotlin("test", embeddedKotlinVersion))

    "functionalTestImplementation"(platform(libs.org.junit.junit.bom))
}
