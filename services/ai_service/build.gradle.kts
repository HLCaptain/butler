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
    mainClass = "illyan.butler.services.ai.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName = "butler_ai_service.jar"
    }
}

buildConfig {
    packageName = "illyan.butler.services.ai"
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
    implementation(libs.ktor.server.cio)
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
    implementation(libs.opentelemetry.resources)

    implementation(libs.spring.security.crypto)
    implementation(libs.bouncycastle)
    implementation(libs.napier)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.auth)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.ktor.client.websockets)

    // OpenAI Client
    implementation(libs.openai.client)
    implementation(libs.ktor.client.okhttp)

    // Database
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)

    // Cache
    implementation(libs.redisson)

    // Koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)
    implementation(platform(libs.koin.annotations.bom))
    implementation(libs.koin.annotations)
    implementation("io.ktor:ktor-client-core-jvm:2.3.10")
    implementation("io.ktor:ktor-client-apache:2.3.10")
    ksp(libs.koin.ksp.compiler)

    // Tests
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
