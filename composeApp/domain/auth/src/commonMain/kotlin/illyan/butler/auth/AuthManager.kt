package illyan.butler.auth

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.data.user.UserRepository
import org.koin.core.annotation.Single

@Single
class AuthManager(
    private val userRepository: UserRepository,
    private val resourceLocalDataSource: ResourceLocalDataSource,
    private val chatLocalDataSource: ChatLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource,
) {
    val isUserSignedIn = userRepository.isUserSignedIn
    val signedInUserId = userRepository.signedInUserId
    val signedInUserEmail = userRepository.signedInUserEmail
    val signedInUserPhoneNumber = userRepository.signedInUserPhoneNumber
    val signedInUserPhotoURL = userRepository.signedInUserPhotoURL
    val signedInUserName = userRepository.signedInUserName

    val isUserSigningIn = userRepository.isUserSigningIn

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ) = userRepository.loginWithEmailAndPassword(email, password)

    suspend fun signUpAndLogin(
        email: String,
        password: String,
        userName: String
    ) = userRepository.signUpAndLogin(email, password, userName)

    suspend fun sendPasswordResetEmail(email: String) = userRepository.sendPasswordResetEmail(email)
    suspend fun signOut() {
        // Delete everything from the database
        userRepository.signOut()
        messageLocalDataSource.deleteAllMessages()
        chatLocalDataSource.deleteAllChats()
        resourceLocalDataSource.deleteAllResources()
    }

    suspend fun deleteAccount() {
        // TODO: Implement delete account by calling repositories and signing out user
    }
}