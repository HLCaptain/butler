package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.di.apiHosts
import ai.nest.api_gateway.di.defaultRequestContentType
import ai.nest.api_gateway.di.developmentMode
import ai.nest.api_gateway.utils.APIs
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.util.Attributes

fun Application.configureAttributes(attributes: Attributes) {
    attributes.developmentMode = environment.config.property("ktor.development").getString() == "true"
    val defaultContentType = environment.config.property("ktor.defaultContentType").getString()
    attributes.defaultRequestContentType = ContentType.parse(defaultContentType)
    attributes.apiHosts = APIs.entries.associate { it.key to environment.config.property("apis.${it.key}").getString() }
}
