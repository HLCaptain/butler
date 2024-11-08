package illyan.butler.repository.user

import illyan.butler.core.utils.randomUUID
import illyan.butler.data.user.UserRepository
import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
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
class UserMemoryRepository : UserRepository {
    private val _isUserSigningIn = MutableStateFlow(false)

    private val _userData = MutableStateFlow<DomainUser?>(null)
    override val userData: StateFlow<DomainUser?> = _userData.asStateFlow()
    override val isUserSignedIn = userData.map { it != null }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserId = userData.map { it?.id }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
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