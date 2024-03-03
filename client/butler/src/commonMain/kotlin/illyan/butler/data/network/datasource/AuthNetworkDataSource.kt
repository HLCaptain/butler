package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.network.model.identity.UserDetailsDto
import kotlinx.coroutines.flow.Flow

interface AuthNetworkDataSource {
    suspend fun signup(credentials: UserRegistrationDto): UserDetailsDto
    suspend fun login(credentials: UserLoginDto): UserTokensResponse
    suspend fun sendPasswordResetEmail(request: PasswordResetRequest)
    suspend fun getMe(): Flow<UserDetailsDto>
}
