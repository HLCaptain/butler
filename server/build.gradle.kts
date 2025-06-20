plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.buildconfig)
    application
}

group = "illyan"
version = "0.0.1"
val apiVersion = 1

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "illyan.butler.server.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName = "butler_server.jar"
    }
}

buildConfig {
    packageName = "illyan.butler.server"
    buildConfigField("String", "API_VERSION", "\"$apiVersion\"")
    buildConfigField("String", "PROJECT_VERSION", "\"$version\"")
    buildConfigField("String", "PROJECT_NAME", "\"${project.name}\"")
    buildConfigField("String", "PROJECT_GROUP", "\"$group\"")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.shared.model)
    implementation(projects.shared.llm)

    // Kotlin
    implementation(libs.kotlinx.datetime)

    // Ktor Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    // Ktor Server
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.headers)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.sse)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test)
    implementation(libs.logback.classic)

    // Koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.core)
    implementation(platform(libs.koin.annotations.bom))
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp)

    // Database
    implementation(libs.postgresql.r2dbc)
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.jdbc) // Required for SchemaUtils
    implementation(libs.exposed.dao)
    implementation(libs.exposed.json)

    // Security
    implementation(libs.commons.codec)
    implementation(libs.spring.security.crypto)
    implementation(libs.bouncycastle) // Required for spring.security.crypto

    // Ktor Client
    implementation(libs.ktor.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.encoding)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.ktor.client.websockets)

    // OpenTelemetry
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.exporter.prometheus)
    implementation(libs.opentelemetry.trace)
    implementation(libs.opentelemetry.autoconfigure)
    implementation(libs.opentelemetry.ktor)
    implementation(libs.opentelemetry.resources)

    // Ktor Metrics
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.napier)

    implementation(libs.openai.client)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
