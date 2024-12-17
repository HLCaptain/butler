package illyan.butler.data.user

import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class UserMemoryRepository : UserRepository {
    private val _isUserSigningIn = MutableStateFlow(false)

    private val _userData = MutableStateFlow<DomainUser?>(null)
    override val userData: Flow<DomainUser?> = _userData.asStateFlow()
    override val isUserSignedIn = userData.map { it != null }
    override val signedInUserId = userData.map { it?.id }
    override val signedInUserEmail = userData.map { it?.email }
    override val signedInUserPhoneNumber = userData.map { it?.phone }
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }
    override val signedInUserName = userData.map { it?.username }
    override val isUserSigningIn = _isUserSigningIn.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun loginWithEmailAndPassword(email: String, password: String) {
        _userData.update {
            DomainUser(
                id = Uuid.random().toString(),
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

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun signUpAndLogin(email: String, password: String, userName: String) {
        _userData.update {
            DomainUser(
                id = Uuid.random().toString(),
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