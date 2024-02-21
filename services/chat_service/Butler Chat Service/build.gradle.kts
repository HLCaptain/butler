plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ksp)
}

group = "illyan"
version = "0.0.1"
val apiVersion = 1

application {
    mainClass = "illyan.butler.services.chat.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName = "butler_chat_service.jar"
    }
}

buildConfig {
    buildConfigField("String", "API_VERSION", "\"$apiVersion\"")
    buildConfigField("String", "PROJECT_VERSION", "\"$version\"")
    buildConfigField("String", "PROJECT_NAME", "\"${project.name}\"")
    buildConfigField("String", "PROJECT_GROUP", "\"$group\"")
}

application {
    mainClass.set("ai.nest.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.websockets.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.ktor.server.swagger.jvm)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.metrics.jvm)
    implementation(libs.ktor.server.call.id.jvm)
    implementation(libs.ktor.server.metrics.micrometer.jvm)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.ktor.server.compression.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback.classic)

    // Tests
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
