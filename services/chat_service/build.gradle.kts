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
    packageName = "illyan.butler.services.chat"
    buildConfigField("String", "API_VERSION", "\"$apiVersion\"")
    buildConfigField("String", "PROJECT_VERSION", "\"$version\"")
    buildConfigField("String", "PROJECT_NAME", "\"${project.name}\"")
    buildConfigField("String", "PROJECT_GROUP", "\"$group\"")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.headers)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.nanoid)
    implementation(libs.kotlinx.datetime)

    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.exporter.prometheus)
    implementation(libs.opentelemetry.trace)
    implementation(libs.opentelemetry.autoconfigure)
    implementation(libs.opentelemetry.ktor)

    // Database
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.json)

    implementation(libs.napier)

    // Cache
    implementation(libs.redisson)

    // Koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)
    implementation(platform(libs.koin.annotations.bom))
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    // Tests
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
