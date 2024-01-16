package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.di.apiHosts
import ai.nest.api_gateway.di.defaultRequestContentType
import ai.nest.api_gateway.di.developmentMode
import ai.nest.api_gateway.utils.APIs
import io.ktor.server.application.Application
import io.ktor.util.Attributes

fun Application.configureAttributes(attributes: Attributes) {
    attributes.developmentMode = environment.config.property("ktor.development").getString() == "true"
    attributes.defaultRequestContentType = environment.config.property("ktor.defaultContentType").getString()
    attributes.apiHosts = APIs.entries.associate { it.key to environment.config.property("apis.${it.key}").getString() }
}

// FIXME:
//  - Set HttpClient development mode
//  - Set HttpClient default request url host dynamically
//  - May use @Factory annotation to create HttpClient
//  - Try to rely on environment.config instead of System.getenv
//  - Accept JSON and Protobuf content types
//  - Request JSON and Protobuf content types
//  - If development mode is enabled, request JSON, otherwise request the default content type set in config
//  - Set developmentMode to true if ktor.development is set to true in application.yaml
//  - Try to remove usage of hardcoded Attribute key and System.getenv
