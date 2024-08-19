package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.AuthService
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
class AuthRpcDataSource(
    private val authService: StateFlow<AuthService?>,
) : AuthNetworkDataSource {
    override suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto {
        return authService.value?.signup(credentials) ?: throw IllegalStateException("AuthService is not available")
    }

    override suspend fun login(credentials: UserLoginDto): UserLoginResponseDto {
        return authService.value?.login(credentials) ?: throw IllegalStateException("AuthService is not available")
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
        return authService.value?.sendPasswordResetEmail(request) ?: throw IllegalStateException("AuthService is not available")
    }
}