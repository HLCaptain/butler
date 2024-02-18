package illyan.butler.di

import illyan.butler.isDebugBuild
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single

@OptIn(ExperimentalSerializationApi::class)
@Single
fun provideHttpClient() = HttpClient {
    install(WebSockets)
    install(ContentNegotiation) {
        json()
        protobuf()
    }

    developmentMode = isDebugBuild()

    val fallbackPlugin = createClientPlugin("ContentTypeFallback", ::ContentTypeFallbackConfig) {
        on(Send) { request ->
            when (request.body) {
                is OutgoingContent -> {
                    try {
                        if (pluginConfig.supportedContentTypes.isEmpty()) throw IllegalStateException("No supported content types. Please add at least one content type to the supportedContentTypes list in the ContentTypeFallbackConfig.")
                        pluginConfig.supportedContentTypes.firstNotNullOf {
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
            listOf(fallbackContentType, defaultContentType)
        } else {
            listOf(fallbackContentType)
        }
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}