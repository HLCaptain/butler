package illyan.butler.services.ai.di

import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.data.utils.WebsocketContentConverterWithFallback
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.util.Attributes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@Single
fun provideHttpClientAttribute(): Attributes {
    return Attributes(true)
}

@OptIn(ExperimentalSerializationApi::class)
@Single
fun provideHttpClient() = HttpClient(CIO) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { throwable, _ ->
            Napier.e(throwable) { "Error in response" }
        }
    }

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }

    developmentMode = AppConfig.Ktor.DEVELOPMENT

    install(WebSockets) {
        contentConverter = WebsocketContentConverterWithFallback(
            listOf(
                KotlinxWebsocketSerializationConverter(ProtoBuf),
                KotlinxWebsocketSerializationConverter(Json)
            )
        )
    }

    val fallbackPlugin = createClientPlugin("ContentTypeFallback", ::ContentTypeFallbackConfig) {
        val contentTypes = pluginConfig.supportedContentTypes
        onRequest { request, content ->
            Napier.v("ContentTypeFallback plugin called onRequest, request: ${request.url}, content: $content")
            // Default body is EmptyContent
            // Don't set content type if content itself is not set
            if (request.contentType() == null && content !is EmptyContent) {
                Napier.v("Request content type is null and content is not EmptyContent, setting content type: ${contentTypes.first()}")
                request.contentType(contentTypes.first())
            }
        }
        on(Send) { request ->
            Napier.v("ContentTypeFallback plugin called on(Send)")
            when (request.body) {
                is OutgoingContent -> {
                    try {
                        if (contentTypes.isEmpty()) throw IllegalStateException("No supported content types. Please add at least one content type to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                        contentTypes.firstNotNullOf {
                            request.contentType(it)
                            val call = proceed(request)
                            if (call.response.status != HttpStatusCode.UnsupportedMediaType) call else null
                        }
                    } catch (e: NoSuchElementException) {
                        throw IllegalStateException("Server does not support any of the content types in the supportedContentTypes list configured in ContentTypeFallbackConfig. Please add at least one server supported content type to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                    }
                }
                else -> proceed(request)
            }
        }
    }

    install(fallbackPlugin) {
        val fallbackContentType = ContentType.Application.Json
        val defaultContentType = ContentType.Application.ProtoBuf
        supportedContentTypes = if (developmentMode) {
            listOf(fallbackContentType)
        } else {
            listOf(defaultContentType, fallbackContentType)
        }
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }

    install(ContentEncoding)
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}

// TODO: add fallback to WebSocket serialization
