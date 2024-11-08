package illyan.butler.data.user

import illyan.butler.core.local.room.dao.AppSettingsDao
import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import illyan.butler.shared.model.auth.PasswordResetRequest
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class UserRoomRepository(
    private val userDao: UserDao,
    private val appSettingsDao: AppSettingsDao,
    private val authNetworkDataSource: AuthNetworkDataSource,
) : UserRepository {
    override val userData: StateFlow<DomainUser?> = userDao.getCurrentUser().map { it?.toDomainModel() }
        .stateIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            null
        )

    override val isUserSignedIn = userDao.isUserSignedIn().stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserId = userData.map { it?.id }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)

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
        userDao.updateTokens(accessToken?.toRoomModel(), refreshToken?.toRoomModel())
    }
}
