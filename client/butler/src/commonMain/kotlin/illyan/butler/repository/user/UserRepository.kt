package illyan.butler.repository.user

import illyan.butler.domain.model.DomainUser
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_AUTH_PROVIDER = "auth_provider"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACCESS_TOKEN_EXPIRATION = "access_token_expiration"
        const val KEY_REFRESH_TOKEN_EXPIRATION = "refresh_token_expiration"
        const val FIRST_SIGN_IN_HAPPENED_YET = "FIRST_SIGN_IN_HAPPENED_YET"
    }

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
}
