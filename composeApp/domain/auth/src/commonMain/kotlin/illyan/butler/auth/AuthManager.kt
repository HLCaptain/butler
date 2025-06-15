package illyan.butler.auth

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.settings.AppRepository
import illyan.butler.data.user.UserRepository
import illyan.butler.data.user.toDomainModel
import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class AuthManager(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val credentialRepository: CredentialRepository,
    private val resourceLocalDataSource: ResourceLocalDataSource,
    private val chatLocalDataSource: ChatLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource,
) {
    val clientId = appRepository.appSettings.map { it?.clientId }
    val isUserSignedIn = appRepository.isUserSignedIn
    val signedInServers = appRepository.signedInServers

    private val _isUserSigningIn = MutableStateFlow(false)
    val isUserSigningIn = _isUserSigningIn.asStateFlow()

    private suspend fun setLoggedInUser(response: UserLoginResponseDto, endpoint: String) {
        Napier.d("Setting logged in user: $response")
        val tokens = response.tokensResponse
        userRepository.upsertUser(response.user.toDomainModel(tokens, endpoint))
        appRepository.addServerSource(
            Source.Server(
                userId = response.user.id,
                endpoint = endpoint
            )
        )
    }

    fun getUser(userId: Uuid) = userRepository.getUser(userId)

    suspend fun login(email: String, password: String, endpoint: String) {
        try {
            _isUserSigningIn.update { true }
            setLoggedInUser(authNetworkDataSource.login(UserLoginDto(email, password), endpoint), endpoint)
        } finally { _isUserSigningIn.update { false } }
    }

    suspend fun signUpAndLogin(email: String, password: String, endpoint: String) {
        try {
            _isUserSigningIn.update { true }
            setLoggedInUser(authNetworkDataSource.signup(UserRegistrationDto(email, password), endpoint), endpoint)
        } finally { _isUserSigningIn.update { false } }
    }

    suspend fun signOut(source: Source.Server) {
        // Delete everything from the database
        appRepository.removeServerSource(source)
        userRepository.deleteUser(source.userId)
        messageLocalDataSource.deleteAllMessages()
        chatLocalDataSource.deleteAllChats()
        resourceLocalDataSource.deleteAllResources()
    }

    suspend fun upsertCredential(apiKeyCredential: ApiKeyCredential) {
        credentialRepository.upsertApiKeyCredential(apiKeyCredential)
    }

    suspend fun removeCredential(aiSource: AiSource.Api) {
        credentialRepository.deleteApiKeyCredentialByUrl(aiSource.endpoint)
    }
}
