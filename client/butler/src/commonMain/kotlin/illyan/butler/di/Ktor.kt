package illyan.butler.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.ktor.utils.WebsocketContentConverterWithFallback
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.isDebugBuild
import illyan.butler.manager.ErrorManager
import illyan.butler.repository.HostRepository
import illyan.butler.repository.UserRepository
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
@Single
fun provideHttpClient(
    settings: FlowSettings,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope,
    errorManager: ErrorManager
) = HttpClient {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { throwable, _ ->
            Napier.e(throwable) { "Error in response" }
            val exception = throwable as? ServerResponseException
            if (exception != null) {
                errorManager.reportError(exception.response)
            } else {
                errorManager.reportError(throwable)
            }
        }
    }

    install(Auth) {
        bearer {
            // DON'T USE HTTP REQUESTS IN `loadTokens`. Only use local storage.
            // loadTokens run every time a request is made.
            loadTokens {
                val accessToken = settings.getString(UserRepository.KEY_ACCESS_TOKEN, "")
                val refreshToken = settings.getString(UserRepository.KEY_REFRESH_TOKEN, "")
                if (accessToken.isBlank() || refreshToken.isBlank()) {
                    Napier.d { "No access or refresh token found in settings" }
                    return@loadTokens null
                }
                BearerTokens(accessToken, refreshToken)
            }
            refreshTokens {
                val refreshTokenInfo = client.post("/refresh-access-token").body<UserTokensResponse>()
                val newToken = BearerTokens(refreshTokenInfo.accessToken, refreshTokenInfo.refreshToken)
                settings.putString(UserRepository.KEY_ACCESS_TOKEN, refreshTokenInfo.accessToken)
                settings.putString(UserRepository.KEY_REFRESH_TOKEN, refreshTokenInfo.refreshToken)
                settings.putLong(UserRepository.KEY_ACCESS_TOKEN_EXPIRATION, refreshTokenInfo.accessTokenExpirationMillis)
                settings.putLong(UserRepository.KEY_REFRESH_TOKEN_EXPIRATION, refreshTokenInfo.refreshTokenExpirationMillis)
                newToken
            }
        }
    }

    install(WebSockets) {
        contentConverter = WebsocketContentConverterWithFallback(
            listOf(
                KotlinxWebsocketSerializationConverter(ProtoBuf),
                KotlinxWebsocketSerializationConverter(Json)
            )
        )
    }

    developmentMode = isDebugBuild()

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

    var currentApiUrl: String? = null
    coroutineScopeIO.launch {
        settings.getStringOrNullFlow(HostRepository.KEY_API_URL).collectLatest {
            Napier.d { "API URL changed to $it" }
            currentApiUrl = it
        }
    }
    defaultRequest {
        Napier.v("Default request interceptor called, currentApiUrl: $currentApiUrl")
        url(urlString = currentApiUrl ?: "")
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}