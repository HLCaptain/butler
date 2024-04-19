package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.identity.UserDto
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class UserSettingsRepository(
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val settings: FlowSettings,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : UserRepository {
    /**
     * User data and auth state listed by property
     */
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    override val userData = settings.getStringOrNullFlow(UserRepository.KEY_USER_ID).map { encodedUser ->
        encodedUser?.let {
            ProtoBuf.decodeFromHexString<UserDto>(encodedUser).also { Napier.d("User data: $it") }
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val isUserSignedIn = userData.map { it != null }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserUUID = userData.map { it?.id }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val _isUserSigningIn = MutableStateFlow(false)
    override val isUserSigningIn = _isUserSigningIn.asStateFlow()

    override suspend fun loginWithEmailAndPassword(email: String, password: String) {
        try {
            _isUserSigningIn.update { true }
            val response = authNetworkDataSource.login(UserLoginDto(email, password))
            setLoggedInUser(response)
        } finally { _isUserSigningIn.update { false } }
    }

    override suspend fun signUpAndLogin(email: String, password: String, userName: String) {
        try {
            _isUserSigningIn.update { true }
            authNetworkDataSource.signup(UserRegistrationDto(email, password, userName)).also {
                setLoggedInUser(it)
            }
        } finally { _isUserSigningIn.update { false } }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun setLoggedInUser(response: UserLoginResponseDto) {
        Napier.d("Setting logged in user: $response")
        val tokens = response.tokensResponse
        settings.putString(UserRepository.KEY_AUTH_PROVIDER, "butler_api")
        settings.putString(UserRepository.KEY_ACCESS_TOKEN, tokens.accessToken)
        settings.putString(UserRepository.KEY_REFRESH_TOKEN, tokens.refreshToken)
        settings.putLong(UserRepository.KEY_ACCESS_TOKEN_EXPIRATION, tokens.accessTokenExpirationMillis)
        settings.putLong(UserRepository.KEY_REFRESH_TOKEN_EXPIRATION, tokens.refreshTokenExpirationMillis)
        settings.putBoolean(UserRepository.FIRST_SIGN_IN_HAPPENED_YET, true)
        settings.putString(UserRepository.KEY_USER_ID, ProtoBuf.encodeToHexString(response.user))
//        val me = authNetworkDataSource.getMe().first() // TODO: listen to this and update the user data dynamically
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        authNetworkDataSource.sendPasswordResetEmail(PasswordResetRequest(email))
    }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun signOut() {
        settings.remove(UserRepository.KEY_AUTH_PROVIDER)
        settings.remove(UserRepository.KEY_USER_ID)
        settings.remove(UserRepository.KEY_ACCESS_TOKEN)
        settings.remove(UserRepository.KEY_REFRESH_TOKEN)
        settings.remove(UserRepository.KEY_ACCESS_TOKEN_EXPIRATION)
        settings.remove(UserRepository.KEY_REFRESH_TOKEN_EXPIRATION)
    }

    override suspend fun deleteUserData() {
        settings.remove(UserRepository.FIRST_SIGN_IN_HAPPENED_YET)
        signOut()
    }
}