package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.di.apiHosts
import ai.nest.api_gateway.di.defaultRequestContentType
import ai.nest.api_gateway.utils.APIs
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.util.Attributes

fun Application.configureAttributes(attributes: Attributes) {
    attributes.defaultRequestContentType = ContentType.parse(AppConfig.Ktor.DEFAULT_CONTENT_TYPE)
    attributes.apiHosts = APIs.entries.associate { it.key to it.url }
}
