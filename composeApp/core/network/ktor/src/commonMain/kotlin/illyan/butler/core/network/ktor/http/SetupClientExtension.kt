@file:OptIn(ExperimentalTime::class)

package illyan.butler.core.network.ktor.http

import illyan.butler.config.BuildConfig
import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.data.error.ErrorRepository
import illyan.butler.domain.model.Token
import illyan.butler.shared.model.response.UserTokensResponse
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.network.tls.CIOCipherSuites
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun HttpClientConfig<CIOEngineConfig>.setupCioClient() {
    engine {
        https {
            serverName = null
            cipherSuites = CIOCipherSuites.SupportedSuites
        }
    }
}

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
fun HttpClientConfig<*>.setupClient(
    userDataSource: UserLocalDataSource,
    errorRepository: ErrorRepository,
    endpoint: String,
    userId: Uuid?,
) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { throwable, _ ->
            Napier.e(throwable) { "Error in response" }

            when (throwable) {
                is ServerResponseException -> errorRepository.reportError(throwable.response)
                is ClientRequestException -> errorRepository.reportError(throwable.response)
                else -> Napier.e { "Unhandled exception: $throwable" } // Do not report, just log
            }
        }
    }

    install(ContentNegotiation) {
        json()
        protobuf()
    }

    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                Napier.v(message, null, "HTTP Client")
            }
        }
        level = LogLevel.HEADERS
    }

    install(Auth) {
        bearer {
            // DON'T USE HTTP REQUESTS IN `loadTokens`. Only use local storage.
            // loadTokens run every time a request is made.
            loadTokens {
                Napier.v { "Loading tokens" }
                if (userId == null) {
                    Napier.d { "User is not signed in" }
                    null
                } else {
                    val currentUser = userDataSource.getUser(userId).first()
                    val accessMillis = currentUser?.accessToken?.tokenExpiration?.toEpochMilliseconds()
                    val refreshMillis = currentUser?.refreshToken?.tokenExpiration?.toEpochMilliseconds()
                    val accessToken = currentUser?.accessToken?.token
                    val refreshToken = currentUser?.refreshToken?.token
                    val currentMillis = Clock.System.now().toEpochMilliseconds()
                    if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                        Napier.d { "No access or refresh token found in app settings" }
                        null
                    } else if (accessMillis == null ||
                        accessMillis < currentMillis ||
                        refreshMillis == null ||
                        refreshMillis < currentMillis
                    ) {
                        Napier.d { "Access or refresh token expired" }
                        BearerTokens(accessToken, refreshToken)
                    } else {
                        Napier.d { "Access and refresh tokens found in app settings" }
                        BearerTokens(accessToken, refreshToken)
                    }
                }
            }
            refreshTokens {
                if (userId != null) {
                    val currentUser = userDataSource.getUser(userId).first()
                    val accessMillis = currentUser?.accessToken?.tokenExpiration?.toEpochMilliseconds()
                    val refreshMillis = currentUser?.refreshToken?.tokenExpiration?.toEpochMilliseconds()
                    val accessToken = currentUser?.accessToken?.token
                    val refreshToken = currentUser?.refreshToken?.token
                    val currentMillis = Clock.System.now().toEpochMilliseconds()
                    if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                        Napier.d { "No access or refresh token found in app settings" }
                        null
                    } else if (accessMillis == null ||
                        accessMillis < currentMillis ||
                        refreshMillis == null ||
                        refreshMillis < currentMillis
                    ) {
                        Napier.d { "Access or refresh token expired, refreshing tokens" }
                        client.post("/refresh-access-token") {
                            oldTokens?.refreshToken?.let { bearerAuth(it) }
                        }.body<UserTokensResponse>().run {
                            userDataSource.refreshUserTokens(
                                userId,
                                Token(accessToken, Instant.fromEpochMilliseconds(accessTokenExpirationMillis)),
                                Token(refreshToken, Instant.fromEpochMilliseconds(refreshTokenExpirationMillis))
                            )
                            BearerTokens(accessToken, refreshToken)
                        }
                    } else {
                        Napier.d { "Access and refresh tokens found in app settings" }
                        BearerTokens(accessToken, refreshToken)
                    }
                } else {
                    Napier.d { "User is not signed in, not refreshing tokens" }
                    null
                }
            }
        }
    }

    val fallbackPlugin = createClientPlugin("ContentTypeFallback", ::ContentTypeFallbackConfig) {
        val contentTypes = pluginConfig.supportedContentTypes
        onRequest { request, content ->
            Napier.v("ContentTypeFallback plugin called onRequest, request: ${request.url}, content: $content")
            // Default body is EmptyContent
            // Don't set content mimeType if content itself is not set
            if (request.contentType() == null && content !is EmptyContent) {
                Napier.v("Request content mimeType is null and content is not EmptyContent, setting content mimeType: ${contentTypes.first()}")
                request.contentType(contentTypes.first())
            }
        }
        on(Send) { request ->
            Napier.v("ContentTypeFallback plugin called on(Send)")
            when (request.body) {
                is OutgoingContent -> {
                    try {
                        if (contentTypes.isEmpty()) throw IllegalStateException("No supported content types. Please add at least one content mimeType to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                        contentTypes.firstNotNullOf {
                            request.contentType(it)
                            val call = proceed(request)
                            if (call.response.status != HttpStatusCode.UnsupportedMediaType) call else null
                        }
                    } catch (e: NoSuchElementException) {
                        throw IllegalStateException("Server does not support any of the content types in the supportedContentTypes list configured in ContentTypeFallbackConfig. Please add at least one server supported content mimeType to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                    }
                }
                else -> proceed(request)
            }
        }
    }

    install(fallbackPlugin) {
        val fallbackContentType = ContentType.Application.Json
        val defaultContentType = ContentType.Application.ProtoBuf
        supportedContentTypes = if (BuildConfig.DEBUG) {
            listOf(fallbackContentType)
        } else {
            listOf(defaultContentType, fallbackContentType)
        }
    }

    install(ContentEncoding) {
        // Enable gzip compression for requests and responses
        deflate(1f)
        gzip(0.9f)
    }

    defaultRequest {
        Napier.v("Default request interceptor called, currentApiUrl: ${endpoint}, protocol: ${url.protocol}")
        url(urlString = endpoint)
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}
