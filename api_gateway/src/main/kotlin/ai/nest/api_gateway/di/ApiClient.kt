package ai.nest.api_gateway.di

import ai.nest.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlinx.serialization.ExperimentalSerializationApi
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

    developmentMode = AppConfig.Ktor.DEVELOPMENT

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(AppConfig.Ktor.SERIALIZATION_FORMAT)
        pingInterval = 5.seconds.inWholeMilliseconds
    }

    defaultRequest {
        url(attributes.apiHosts[attributes.apiKeyToRequestFrom])
    }

    val fallbackPlugin = createClientPlugin("ContentTypeFallback", ::ContentTypeFallbackConfig) {
        on(Send) { request ->
            when (request.body) {
                is OutgoingContent -> {
                    try {
                        if (pluginConfig.supportedContentTypes.isEmpty()) throw IllegalStateException("No supported content types.\nPlease add at least one content type to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                        pluginConfig.supportedContentTypes.firstNotNullOf {
                            request.contentType(it)
                            val call = proceed(request)
                            if (call.response.status != HttpStatusCode.UnsupportedMediaType) call else null
                        }
                    } catch (e: NoSuchElementException) {
                        throw IllegalStateException("Server does not support any of the content types in the supportedContentTypes list configured in ContentTypeFallbackConfig.\nPlease add at least one server supported content type to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                    }
                }
                else -> proceed(request)
            }
        }
    }

    install(fallbackPlugin) {
        supportedContentTypes = AppConfig.Ktor.SUPPORTED_CONTENT_TYPES
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}

// TODO: add fallback to WebSocket serialization

var Attributes.apiHosts: Map<String, String>
    get() = getOrNull(AttributeKey("apiHosts")) ?: emptyMap()
    set(value) = put(AttributeKey("apiHosts"), value)

var Attributes.apiKeyToRequestFrom: String
    get() = get(AttributeKey("apiKey"))
    set(value) = put(AttributeKey("apiKey"), value)
