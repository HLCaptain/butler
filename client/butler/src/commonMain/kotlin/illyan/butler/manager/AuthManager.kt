package illyan.butler.manager

import illyan.butler.repository.UserRepository
import org.koin.core.annotation.Single

@Single
class AuthManager(
    private val userRepository: UserRepository
) {
    val isUserSignedIn = userRepository.isUserSignedIn
    val signedInUserUUID = userRepository.signedInUserUUID
    val signedInUserEmail = userRepository.signedInUserEmail
    val signedInUserPhoneNumber = userRepository.signedInUserPhoneNumber
    val signedInUserPhotoURL = userRepository.signedInUserPhotoURL
    val signedInUserName = userRepository.signedInUserName

    val isUserSigningIn = userRepository.isUserSigningIn

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ) = userRepository.loginWithEmailAndPassword(email, password)

    suspend fun createUserWithEmailAndPassword(
        email: String,
        userName: String,
        password: String
    ) = userRepository.createUserWithEmailAndPassword(email, userName, password)

    suspend fun sendPasswordResetEmail(email: String) = userRepository.sendPasswordResetEmail(email)
    suspend fun signOut() = userRepository.signOut()

    suspend fun deleteAccount() {
        // TODO: Implement delete account by calling repositories and signing out user
    }
}