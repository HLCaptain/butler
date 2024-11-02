package illyan.butler.repository.user

import illyan.butler.data.local.room.dao.AppSettingsDao
import illyan.butler.data.local.room.dao.UserDao
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.di.KoinNames
import illyan.butler.model.DomainToken
import illyan.butler.model.DomainUser
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class UserRoomRepository(
    private val userDao: UserDao,
    private val appSettingsDao: AppSettingsDao,
    private val authNetworkDataSource: AuthNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : UserRepository {
    override val userData: StateFlow<DomainUser?> = userDao.getCurrentUser().map { it?.toDomainModel() }
        .stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            null
        )

    override val isUserSignedIn = userDao.isUserSignedIn().stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    override val signedInUserId = userData.map { it?.id }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

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

    private suspend fun setLoggedInUser(response: UserLoginResponseDto) {
        Napier.d("Setting logged in user: $response")
        val tokens = response.tokensResponse
        userDao.upsertUser(response.user.toRoomModel(tokens))
        appSettingsDao.updateFirstSignInHappenedYet(true)
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        authNetworkDataSource.sendPasswordResetEmail(PasswordResetRequest(email))
    }

    override suspend fun signOut() {
        deleteUserData()
    }

    override suspend fun deleteUserData() {
        userDao.deleteAllUsers()
        userDao.updateTokens(null, null)
    }

    override suspend fun refreshUserTokens(accessToken: DomainToken?, refreshToken: DomainToken?) {
        userDao.updateTokens(accessToken, refreshToken)
    }
}
