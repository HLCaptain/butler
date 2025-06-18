package illyan.butler.core.network.datasource

import illyan.butler.domain.model.User
import illyan.butler.shared.model.auth.PasswordResetRequest
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow

interface AuthNetworkDataSource {
    /**
     * Signs up a new user based on [UserRegistrationDto].
     * @return details of the new user.
     */
    suspend fun signup(credentials: UserRegistrationDto, endpoint: String): UserLoginResponseDto

    /**
     * Logs in the user.
     * @return new JWT tokens for authentication.
     */
    suspend fun login(credentials: UserLoginDto, endpoint: String): UserLoginResponseDto

    /**
     * Sends a request to reset the password for an email.
     * @return true if reset email is sent.
     */
    suspend fun sendPasswordResetEmail(request: PasswordResetRequest, endpoint: String): Boolean

    fun getUser(source: Source.Server): Flow<User>

    suspend fun updateUserData(user: User): User
}
