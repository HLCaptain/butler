package illyan.butler.di

import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.room.dao.UserDao
import illyan.butler.domain.model.DomainToken
import illyan.butler.manager.ErrorManager
import illyan.butler.repository.app.AppRepository
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
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
import io.ktor.http.URLProtocol
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@ExperimentalSerializationApi
@Single
fun provideHttpClient(
    userDao: UserDao,
    appRepository: AppRepository,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope,
    errorManager: ErrorManager
) = HttpClient {
    setupClient(
        userDao = userDao,
        appRepository = appRepository,
        coroutineScopeIO = coroutineScopeIO,
        errorManager = errorManager
    )

    install(ContentNegotiation) {
        json()
        protobuf()
    }
}

fun HttpClientConfig<*>.setupClient(
    userDao: UserDao,
    appRepository: AppRepository,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope,
    errorManager: ErrorManager
) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { throwable, _ ->
            Napier.e(throwable) { "Error in response" }
            when (throwable) {
                is ServerResponseException -> errorManager.reportError(throwable.response)
                is ClientRequestException -> errorManager.reportError(throwable.response)
                else -> errorManager.reportError(throwable)
            }
        }
    }

    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                Napier.v( message, null, "HTTP Client")
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
                if (!userDao.isUserSignedIn().first()) {
                    Napier.d { "User is not signed in" }
                    null
                } else {
                    val currentUser = userDao.getCurrentUser().filterNotNull().first()
                    val accessMillis = currentUser.accessToken?.tokenExpirationMillis
                    val refreshMillis = currentUser.refreshToken?.tokenExpirationMillis
                    val accessToken = currentUser.accessToken?.token
                    val refreshToken = currentUser.refreshToken?.token
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
                if (userDao.isUserSignedIn().first()) {
                    val currentUser = userDao.getCurrentUser().filterNotNull().first()
                    val accessMillis = currentUser.accessToken?.tokenExpirationMillis
                    val refreshMillis = currentUser.refreshToken?.tokenExpirationMillis
                    val accessToken = currentUser.accessToken?.token
                    val refreshToken = currentUser.refreshToken?.token
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
                        Napier.d { "Refreshing tokens" }
                        client.post("/refresh-access-token") {
                            oldTokens?.refreshToken?.let { bearerAuth(it) }
                        }.body<UserTokensResponse>().run {
                            userDao.updateTokens(
                                DomainToken(accessToken, accessTokenExpirationMillis),
                                DomainToken(refreshToken, refreshTokenExpirationMillis)
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

    //    developmentMode = isDebugBuild()
    developmentMode = true

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

    install(ContentEncoding)

    var currentApiUrl: String? = null
    coroutineScopeIO.launch {
        appRepository.currentHost.collectLatest {
            Napier.d { "API URL changed to $it" }
            currentApiUrl = it
        }
    }
    defaultRequest {
        Napier.v("Default request interceptor called, currentApiUrl: $currentApiUrl, protocol: ${url.protocol}")
        url(urlString = currentApiUrl ?: "http://localhost:8080")
    }
}

class ContentTypeFallbackConfig {
    /**
     * Order matters! First is the default serialization we want to use.
     */
    var supportedContentTypes: List<ContentType> = emptyList()
}