package illyan.butler.auth

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.settings.AppRepository
import illyan.butler.data.user.UserRepository
import illyan.butler.data.user.toDomainModel
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

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
    val signedInUserId = appRepository.currentSignedInUserId
    @OptIn(ExperimentalCoroutinesApi::class)
    val signedInUser = signedInUserId.flatMapLatest { it?.let { userRepository.getUser(it) } ?: flowOf(null) }
    val signedInUserPhotoUrl = signedInUser.map { it?.photoUrl }
    val signedInUserDisplayName = signedInUser.map { it?.displayName }
    val signedInUserPhoneNumber = signedInUser.map { it?.phone }
    val signedInUserEmail = signedInUser.map { it?.email }

    private val _isUserSigningIn = MutableStateFlow(false)
    val isUserSigningIn = _isUserSigningIn.asStateFlow()

    private suspend fun setLoggedInUser(response: UserLoginResponseDto) {
        Napier.d("Setting logged in user: $response")
        val tokens = response.tokensResponse
        userRepository.upsertUser(response.user.toDomainModel(tokens))
        appRepository.setSignedInUser(response.user.id)
    }

    suspend fun login(email: String, password: String) {
        try {
            _isUserSigningIn.update { true }
            setLoggedInUser(authNetworkDataSource.login(UserLoginDto(email, password)))
        } finally { _isUserSigningIn.update { false } }
    }

    suspend fun signUpAndLogin(email: String, password: String) {
        try {
            _isUserSigningIn.update { true }
            setLoggedInUser(authNetworkDataSource.signup(UserRegistrationDto(email, password)))
        } finally { _isUserSigningIn.update { false } }
    }

    suspend fun signOut(userId: String? = null) {
        val currentUser = userId ?: signedInUserId.first() ?: throw IllegalStateException("User is not signed in")
        // Delete everything from the database
        appRepository.setSignedInUser(null)
        userRepository.deleteUserData(currentUser)
        messageLocalDataSource.deleteAllMessages()
        chatLocalDataSource.deleteAllChats()
        resourceLocalDataSource.deleteAllResources()
    }

    suspend fun upsertCredential(apiKeyCredential: ApiKeyCredential) {
        credentialRepository.upsertApiKeyCredential(apiKeyCredential)
    }

    suspend fun removeCredential(providerUrl: String) {
        credentialRepository.deleteApiKeyCredentialByUrl(providerUrl)
    }

    suspend fun deleteUserProfile(userId: String) {
        signOut(userId)
        userRepository.deleteUserData(userId)
    }
}
