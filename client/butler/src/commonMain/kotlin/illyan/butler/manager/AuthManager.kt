package illyan.butler.manager

import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.repository.chat.ChatRepository
import illyan.butler.repository.message.MessageRepository
import illyan.butler.repository.resource.ResourceRepository
import illyan.butler.repository.user.UserRepository
import org.koin.core.annotation.Single

@Single
class AuthManager(
    private val userRepository: UserRepository,
    private val databaseHelper: DatabaseHelper
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
        databaseHelper.withDatabase {
            it.resourceQueries.deleteAll()
            it.chatQueries.deleteAll()
            it.messageQueries.deleteAll()
            it.userQueries.deleteAll()
            it.modelQueries.deleteAll()
            it.chatMemberQueries.deleteAll()
            it.errorEventQueries.deleteAll()
        }
        userRepository.signOut()
    }

    suspend fun deleteAccount() {
        // TODO: Implement delete account by calling repositories and signing out user
    }
}