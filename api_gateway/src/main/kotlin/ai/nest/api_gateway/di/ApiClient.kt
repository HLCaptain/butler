package ai.nest.api_gateway.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.appendIfNameAbsent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
fun provideHttpClientAttribute(): Attributes {
    return Attributes(true)
}

@OptIn(ExperimentalSerializationApi::class)
@Single
fun provideHttpClient(attributes: Attributes) = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }

    developmentMode = attributes.developmentMode

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(attributes.serializationFormat)
        pingInterval = 5.seconds.inWholeMilliseconds
    }

    defaultRequest {
        // Check if app is in development mode
        headers.appendIfNameAbsent(HttpHeaders.ContentType, attributes.contentType.toString())
        url(attributes.apiHosts[attributes.apiKeyToRequestFrom])
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }
}

var Attributes.developmentMode: Boolean
    get() = getOrNull(AttributeKey("developmentMode")) ?: false
    set(value) = put(AttributeKey("developmentMode"), value)

var Attributes.defaultRequestContentType: String
    get() = getOrNull(AttributeKey("defaultRequestContentType")) ?: ContentType.Application.Json.toString()
    set(value) = put(AttributeKey("defaultRequestContentType"), value)

val Attributes.contentType: ContentType
    get() = if (developmentMode) ContentType.Application.Json
    else when (defaultRequestContentType) {
        ContentType.Application.Json.toString() -> ContentType.Application.Json
        ContentType.Application.ProtoBuf.toString() -> ContentType.Application.ProtoBuf
        else -> ContentType.Application.Json
    }

@OptIn(ExperimentalSerializationApi::class)
val Attributes.serializationFormat: SerialFormat
    get() = if (developmentMode) Json
    else when (defaultRequestContentType) {
        ContentType.Application.Json.toString() -> Json
        ContentType.Application.ProtoBuf.toString() -> ProtoBuf
        else -> Json
    }

var Attributes.apiHosts: Map<String, String>
    get() = getOrNull(AttributeKey("apiHosts")) ?: emptyMap()
    set(value) = put(AttributeKey("apiHosts"), value)

var Attributes.apiKeyToRequestFrom: String
    get() = get(AttributeKey("apiKey"))
    set(value) = put(AttributeKey("apiKey"), value)
