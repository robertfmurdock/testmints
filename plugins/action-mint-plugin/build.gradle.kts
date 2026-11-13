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
        named("com.zegreatrob.testmints.action-mint") {
            displayName = "Action-Mint Plugin"
            description = "This plugin will generate boilerplate for using 'actions' easily."
            tags.addAll("action", "domain-driven-design", "command", "query", "logging", "testmints", "kotlin")
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$embeddedKotlinVersion"))
    implementation(libs.com.google.devtools.ksp)
    implementation(kotlin("gradle-plugin", embeddedKotlinVersion))
    implementation(kotlin("test", embeddedKotlinVersion))

    "functionalTestImplementation"(platform(libs.org.junit.junit.bom))
}
