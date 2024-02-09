package ai.nest.api_gateway.di

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
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
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
        supportedContentTypes = attributes.supportedContentTypes
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

// TODO: rework serialization methods
//  DEBUG serialization: Should always be JSON
//  PRODUCTION (default) serialization: Should be the defaultRequestContentType
//  FALLBACK serialization: Should be a list of enabled serialization formats used only on the client side

var Attributes.developmentMode: Boolean
    get() = getOrNull(AttributeKey("developmentMode")) ?: false
    set(value) = put(AttributeKey("developmentMode"), value)

var Attributes.defaultRequestContentType: ContentType
    get() = getOrNull(AttributeKey("defaultRequestContentType")) ?: ContentType.Application.Json
    set(value) = put(AttributeKey("defaultRequestContentType"), value)

val Attributes.contentType: ContentType
    get() = if (developmentMode) ContentType.Application.Json
    else defaultRequestContentType

val Attributes.fallbackRequestContentType: ContentType
    get() = ContentType.Application.Json

val Attributes.supportedContentTypes: List<ContentType>
    get() = listOf(defaultRequestContentType, fallbackRequestContentType).distinct()

@OptIn(ExperimentalSerializationApi::class)
val Attributes.serializationFormat: SerialFormat
    get() = if (developmentMode) Json
    else when (defaultRequestContentType) {
        ContentType.Application.Json -> Json
        ContentType.Application.ProtoBuf -> ProtoBuf
        else -> Json
    }

inline fun <reified T> Attributes.encodeContent(content: T): Any {
    return when (serializationFormat) {
        is StringFormat -> (serializationFormat as StringFormat).encodeToString(content)
        is BinaryFormat -> (serializationFormat as BinaryFormat).encodeToByteArray(content)
        else -> Json.encodeToString(content)
    }
}

fun <T> Attributes.decodeContent(deserializationStrategy: DeserializationStrategy<T>, content: Any): T {
    return when (serializationFormat) {
        is StringFormat -> {
            if (content is String)
                (serializationFormat as StringFormat).decodeFromString(deserializationStrategy, content)
            else
                throw IllegalArgumentException("Content is not a String")
        }

        is BinaryFormat -> {
            if (content is ByteArray)
                (serializationFormat as BinaryFormat).decodeFromByteArray(deserializationStrategy, content)
            else
                throw IllegalArgumentException("Content is not a ByteArray")
        }

        else -> {
            if (content is String)
                Json.decodeFromString(deserializationStrategy, content)
            else
                throw IllegalArgumentException("Content is not a String")
        }
    }
}

var Attributes.apiHosts: Map<String, String>
    get() = getOrNull(AttributeKey("apiHosts")) ?: emptyMap()
    set(value) = put(AttributeKey("apiHosts"), value)

var Attributes.apiKeyToRequestFrom: String
    get() = get(AttributeKey("apiKey"))
    set(value) = put(AttributeKey("apiKey"), value)
