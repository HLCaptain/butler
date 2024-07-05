package illyan.butler.repository.user

import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import illyan.butler.utils.randomUUID
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
class UserMemoryRepository(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : UserRepository {
    private val _isUserSigningIn = MutableStateFlow(false)

    private val _userData = MutableStateFlow<DomainUser?>(null)
    override val userData: StateFlow<DomainUser?> = _userData.asStateFlow()
    override val isUserSignedIn = userData.map { it != null }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserId = userData.map { it?.id }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val isUserSigningIn = _isUserSigningIn.asStateFlow()

    override suspend fun loginWithEmailAndPassword(email: String, password: String) {
        _userData.update {
            DomainUser(
                id = randomUUID(),
                email = email,
                phone = null,
                photoUrl = null,
                username = email,
                displayName = email,
                fullName = email,
                address = null,
                refreshToken = null,
                accessToken = null
            )
        }
    }

    override suspend fun signUpAndLogin(email: String, password: String, userName: String) {
        _userData.update {
            DomainUser(
                id = randomUUID(),
                email = email,
                phone = null,
                photoUrl = null,
                username = userName,
                displayName = userName,
                fullName = userName,
                address = null,
                refreshToken = null,
                accessToken = null
            )
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        // This function would need to be modified or removed as it depends on the AuthNetworkDataSource
    }

    override suspend fun signOut() {
        _userData.update { null }
    }

    override suspend fun deleteUserData() {
        _userData.update { null }
    }

    override suspend fun refreshUserTokens(accessToken: DomainToken?, refreshToken: DomainToken?) {
        _userData.update {
            it?.copy(accessToken = accessToken, refreshToken = refreshToken)
        }
    }
}