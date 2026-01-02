plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    application
    id("com.gradleup.shadow") version "9.1.0"
}

group = "org.vengeful.citymanager"
version = "1.0.0"
application {
    mainClass.set("org.vengeful.citymanager.ApplicationKt")
    mainClass = "io.ktor.server.netty.EngineMain"
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.shadowJar {
    archiveBaseName.set("citymanager-server")
    archiveClassifier.set("")
    manifest {
        attributes("Main-Class" to "io.ktor.server.netty.EngineMain")
    }
}

dependencies {
    val ktorVersion = "3.3.0"

    // Main Server
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.config.yaml)

    // Authentication
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // Database
    implementation(libs.exposed.core.v0440)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc.v0440)
    implementation(libs.postgresql.v4260)
    implementation(libs.hikaricp) // для connection pool

    // Web Admin Panel
    implementation(libs.web.core)
    implementation(libs.html.core)
}
