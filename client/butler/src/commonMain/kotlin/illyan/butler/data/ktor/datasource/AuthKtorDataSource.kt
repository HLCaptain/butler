package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.network.model.identity.UserDetailsDto
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class AuthKtorDataSource(
    private val client: HttpClient
) : AuthNetworkDataSource {
    override suspend fun signup(credentials: UserRegistrationDto): UserDetailsDto {
        TODO("Not yet implemented")
    }

    override suspend fun login(credentials: UserLoginDto): UserTokensResponse {
        TODO("Not yet implemented")
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun getMe(): Flow<UserDetailsDto> {
        TODO("Not yet implemented")
    }
}