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
        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
        pingInterval = 5.seconds.inWholeMilliseconds
    }

    defaultRequest {
        // Check if app is in development mode
        if (attributes.developmentMode) {
            ContentType.Application.Json.toString()
        } else {
            try {
                attributes.defaultRequestContentType
            } catch (e: Exception) {
                e.printStackTrace()
                ContentType.Application.Json.toString()
            }
        }.let { contentType ->
            headers.appendIfNameAbsent(HttpHeaders.ContentType, contentType)
        }
        url(attributes.apiHosts[attributes.apiKeyToRequestFrom])
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }
}

var Attributes.developmentMode: Boolean
    get() = getOrNull(AttributeKey("developmentMode")) ?: false
    set(value) { put(AttributeKey("developmentMode"), value) }

var Attributes.defaultRequestContentType: String
    get() = getOrNull(AttributeKey("defaultRequestContentType")) ?: ContentType.Application.Json.toString()
    set(value) { put(AttributeKey("defaultRequestContentType"), value) }

var Attributes.apiHosts: Map<String, String>
    get() = getOrNull(AttributeKey("apiHosts")) ?: emptyMap()
    set(value) { put(AttributeKey("apiHosts"), value) }

var Attributes.apiKeyToRequestFrom: String
    get() = get(AttributeKey("apiKey"))
    set(value) { put(AttributeKey("apiKey"), value) }
