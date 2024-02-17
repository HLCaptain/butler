package illyan.butler.repository

import illyan.butler.data.network.api.AuthApi
import illyan.butler.data.network.model.auth.AuthCredentials
import illyan.butler.data.network.model.auth.PasswordResetRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class UserRepository(
    private val authApi: AuthApi
) {
    private val _token = MutableStateFlow<String?>(null)
    val isUserSignedIn = _token.map { it != null }

    private val _signedInUserUUID = MutableStateFlow<String?>(null)
    val signedInUserUUID = _signedInUserUUID.asStateFlow()

    private val _signedInUserEmail: MutableStateFlow<String?> = MutableStateFlow("todo@todo.com")
    val signedInUserEmail = _signedInUserEmail.asStateFlow()
    private val _signedInUserPhoneNumber: MutableStateFlow<String?> = MutableStateFlow("+1 123-456-7890")
    val signedInUserPhoneNumber = _signedInUserPhoneNumber.asStateFlow()
    private val _signedInUserPhotoURL: MutableStateFlow<String?> = MutableStateFlow("https://picsum.photos/200")
    val signedInUserPhotoURL = _signedInUserPhotoURL.asStateFlow()
    private val _signedInUserName: MutableStateFlow<String?> = MutableStateFlow("Illyan")
    val signedInUserName = _signedInUserName.asStateFlow()

    suspend fun signInWithEmailAndPassword(email: String, password: String) {
        val response = authApi.signIn(AuthCredentials(email, password))
        _token.update { response.token } // Store JWT token for future requests
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Boolean {
        val response = authApi.signUp(AuthCredentials(email, password))
        _token.update { response.token } // Store JWT token
        return true
    }

    suspend fun sendPasswordResetEmail(email: String) {
        authApi.sendPasswordResetEmail(PasswordResetRequest(email))
    }

    suspend fun signOut() {
        authApi.signOut()
        _token.update { null } // Clear token on sign-out
    }
}