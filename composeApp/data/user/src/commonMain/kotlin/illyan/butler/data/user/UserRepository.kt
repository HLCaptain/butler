package illyan.butler.data.user

import illyan.butler.domain.model.DomainToken
import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val userData: Flow<DomainUser?>
    val isUserSignedIn: Flow<Boolean?>
    val signedInUserId: Flow<String?>
    val signedInUserEmail: Flow<String?>
    val signedInUserPhoneNumber: Flow<String?>
    val signedInUserPhotoURL: Flow<String?>
    val signedInUserName: Flow<String?>
    val isUserSigningIn: Flow<Boolean>

    suspend fun loginWithEmailAndPassword(email: String, password: String)
    suspend fun signUpAndLogin(email: String, password: String, userName: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signOut()
    suspend fun deleteUserData()
    suspend fun refreshUserTokens(accessToken: DomainToken?, refreshToken: DomainToken?)
}
