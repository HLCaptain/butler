package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.AuthService
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import org.koin.core.annotation.Single

@Single
class AuthRpcDataSource(
    private val authService: AuthService,
) : AuthNetworkDataSource {
    override suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto {
        return authService.signup(credentials)
    }

    override suspend fun login(credentials: UserLoginDto): UserLoginResponseDto {
        return authService.login(credentials)
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
        return authService.sendPasswordResetEmail(request)
    }
}