package illyan.butler.core.network.ktor.http.di

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.network.ktor.http.setupCioClient
import illyan.butler.core.network.ktor.http.setupClient
import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.error.ErrorManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.network.tls.CIOCipherSuites
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single

@ExperimentalSerializationApi
@Single
fun provideHttpClient(
    userDao: UserDao,
    appRepository: AppRepository,
    errorManager: ErrorManager
): HttpClient = HttpClient(CIO) {
    setupCioClient()
    setupClient(
        userDao = userDao,
        appRepository = appRepository,
        errorManager = errorManager
    )
}

@Single
fun provideOpenAIClient(
    credential: ApiKeyCredential
) = OpenAI(
    config = OpenAIConfig(
        token = credential.apiKey,
        host = OpenAIHost(baseUrl = credential.providerUrl),
        engine = CIO.create {
            https {
                serverName = null // Dynamically infer from URL
                cipherSuites = CIOCipherSuites.SupportedSuites
            }
        }
    )
)
