plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply true
    alias(libs.plugins.kotlin.plugin.serialization)
}


kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "admin.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(compose.html.svg)
                implementation(project(":shared")) // Подключаем общий модуль

                // Для HTTP запросов к твоему Ktor серверу
                implementation("io.ktor:ktor-client-js:3.2.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.2.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.0")
            }
        }
    }
}