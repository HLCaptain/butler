package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.identity.UserDto
import kotlinx.coroutines.flow.Flow

interface AuthNetworkDataSource {
    /**
     * Signs up a new user based on [UserRegistrationDto].
     * @return details of the new user.
     */
    suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto

    /**
     * Logs in the user.
     * @return new JWT tokens for authentication.
     */
    suspend fun login(credentials: UserLoginDto): UserLoginResponseDto

    /**
     * Sends a request to reset the password for an email.
     * @return true if reset email is sent.
     */
    suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean

    suspend fun getMe(): Flow<UserDto?>
}
