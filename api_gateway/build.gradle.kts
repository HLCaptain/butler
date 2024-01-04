plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    application
}

group = "ai.nest"
version = "0.0.1"

application {
    mainClass = "ai.nest.api_gateway.ApplicationKt"
}

ktor {
    fatJar {
        archiveFileName = "api_gateway.jar"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.protobuf.jvm)
    implementation(libs.ktor.server.content.negotiation)

    // Ktor Server
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.logback.classic)

    // Koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)
    implementation(platform(libs.koin.annotations.bom))
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

    // Security
    implementation(libs.commons.codec)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
