package illyan.butler.api_gateway.di

import illyan.butler.api_gateway.endpoints.utils.WebsocketContentConverterWithFallback
import illyan.butler.api_gateway.plugins.opentelemetry.client.attributeExtractor
import illyan.butler.api_gateway.plugins.opentelemetry.client.capturedRequestHeaders
import illyan.butler.api_gateway.plugins.opentelemetry.client.capturedResponseHeaders
import illyan.butler.api_gateway.plugins.opentelemetry.client.emitExperimentalHttpClientMetrics
import illyan.butler.api_gateway.plugins.opentelemetry.client.knownMethods
import illyan.butler.api_gateway.utils.AppConfig
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracing
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

fun HttpClientConfig<*>.setupClient() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    developmentMode = AppConfig.Ktor.DEVELOPMENT

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
        supportedContentTypes = AppConfig.Ktor.SUPPORTED_CONTENT_TYPES
    }

    install(KtorClientTracing) {
        setOpenTelemetry(GlobalOpenTelemetry.get())

        emitExperimentalHttpClientMetrics()

        knownMethods(HttpMethod.DefaultMethods)
        capturedRequestHeaders(HttpHeaders.Accept)
        capturedResponseHeaders(HttpHeaders.ContentType)

        attributeExtractor {
            onStart {
                attributes.put("start-time", Clock.System.now().toEpochMilliseconds())
            }
            onEnd {
                attributes.put("end-time", Clock.System.now().toEpochMilliseconds())
            }
        }
    }

    install(ContentEncoding)
}

@Single
fun provideWebSocketClientProvider(): () -> HttpClient = { provideWebSocketClient() }

@Named("WebSocket")
@Single
fun provideWebSocketClient() = HttpClient(OkHttp) {
    setupClient()
    install(WebSockets) {
        contentConverter = WebsocketContentConverterWithFallback(
            AppConfig.Ktor.SERIALIZATION_FORMATS.map { KotlinxWebsocketSerializationConverter(it) }
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Single
fun provideHttpClient() = HttpClient(OkHttp) {
    setupClient()

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
