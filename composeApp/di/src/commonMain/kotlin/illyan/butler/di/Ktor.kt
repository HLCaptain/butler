package illyan.butler.di

import illyan.butler.data.local.room.dao.UserDao
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.settings.AppRepository
import illyan.butler.manager.ErrorManager
import illyan.butler.model.DomainToken
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
import java.util.logging.ErrorManager

@ExperimentalSerializationApi
@Single
fun provideHttpClient(
    userDao: UserDao,
    appRepository: AppRepository,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope,
    errorManager: ErrorManager
): HttpClient = HttpClient {
    setupClient(
        userDao = userDao,
        appRepository = appRepository,
        coroutineScopeIO = coroutineScopeIO,
        errorManager = errorManager
    )
}
