package illyan.butler.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.config.BuildConfig
import illyan.butler.data.ktor.utils.WebsocketContentConverterWithFallback
import illyan.butler.data.network.model.auth.TokenInfo
import illyan.butler.isDebugBuild
import illyan.butler.repository.UserRepository
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.forms.submitForm
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
@Single
fun provideHttpClient(settings: FlowSettings) = HttpClient {
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
            Napier.v("ContentTypeFallback plugin called onRequest, request: $request, content: $content")
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

    install(Auth) {
        bearer {
            loadTokens {
                val accessToken = settings.getStringOrNull(UserRepository.KEY_ACCESS_TOKEN) ?: ""
                val refreshToken = settings.getStringOrNull(UserRepository.KEY_REFRESH_TOKEN) ?: ""
                BearerTokens(accessToken, refreshToken)
            }
            refreshTokens {
                val refreshTokenInfo: TokenInfo = client.submitForm(
                    url = "https://accounts.google.com/o/oauth2/token",
                    formParameters = parameters {
                        append("grant_type", "refresh_token")
                        append("client_id", BuildConfig.GOOGLE_CLIENT_ID)
                        append("refresh_token", oldTokens?.refreshToken ?: "")
                    }
                ) { markAsRefreshTokenRequest() }.body()
                val newToken = BearerTokens(refreshTokenInfo.accessToken, oldTokens?.refreshToken!!)
                settings.putString(UserRepository.KEY_ACCESS_TOKEN, refreshTokenInfo.accessToken)
                settings.putString(UserRepository.KEY_REFRESH_TOKEN, oldTokens?.refreshToken!!)
                newToken
            }
        }
    }

    install(ContentEncoding)

    defaultRequest {
        url(BuildConfig.API_GATEWAY_URL)
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}