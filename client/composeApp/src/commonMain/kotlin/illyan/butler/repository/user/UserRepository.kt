package illyan.butler.repository.user

import illyan.butler.model.DomainToken
import illyan.butler.model.DomainUser
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val userData: StateFlow<DomainUser?>
    val isUserSignedIn: StateFlow<Boolean?>
    val signedInUserId: StateFlow<String?>
    val signedInUserEmail: StateFlow<String?>
    val signedInUserPhoneNumber: StateFlow<String?>
    val signedInUserPhotoURL: StateFlow<String?>
    val signedInUserName: StateFlow<String?>
    val isUserSigningIn: StateFlow<Boolean>

    suspend fun loginWithEmailAndPassword(email: String, password: String)
    suspend fun signUpAndLogin(email: String, password: String, userName: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signOut()
    suspend fun deleteUserData()
    suspend fun refreshUserTokens(accessToken: DomainToken?, refreshToken: DomainToken?)
}
