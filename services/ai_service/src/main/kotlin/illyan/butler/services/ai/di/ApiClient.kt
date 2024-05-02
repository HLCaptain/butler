package illyan.butler.services.ai.di

import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.endpoints.utils.WebsocketContentConverterWithFallback
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes

fun HttpClientConfig<OkHttpConfig>.setupClient() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    developmentMode = AppConfig.Ktor.DEVELOPMENT

    engine {
        config {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?) // Explicitly cast to KeyStore? to indicate null value
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers: ${trustManagers.contentToString()}")
            }
            val trustManager = trustManagers[0] as X509TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
//            val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            sslSocketFactory(sslSocketFactory, trustManager)
        }
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
        supportedContentTypes = AppConfig.Ktor.SUPPORTED_CONTENT_TYPES
    }

    install(ContentEncoding)
}

@Single
fun provideWebSocketClientProvider(): () -> HttpClient = { provideWebSocketClient() }

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

@Named("OpenAIHttpClients")
@Single
fun provideOpenAIHttpClients(): Map<String, HttpClient> {
    return AppConfig.Api.OPEN_AI_API_URLS_AND_KEYS.mapValues { (url, key) ->
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(accessToken = key, refreshToken = "")
                    }
                }
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 10.minutes.inWholeMilliseconds
                connectTimeoutMillis = 10.minutes.inWholeMilliseconds
                requestTimeoutMillis = 10.minutes.inWholeMilliseconds

            }

            install(HttpRequestRetry) {
                maxRetries = 2
                // retry on rate limit error.
                retryIf { _, response -> response.status.value.let { it == 429 } }
                exponentialDelay()
            }

            defaultRequest {
                url(url)
            }

            expectSuccess = true
        }
    }
}
