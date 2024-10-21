package illyan.butler.backend.data.ktor.service.open

import illyan.butler.backend.data.model.authenticate.UserLoginResponseDto
import illyan.butler.backend.data.model.identity.UserLoginDto
import illyan.butler.backend.data.model.identity.UserRegistrationDto
import kotlinx.rpc.RPC

interface OpenAuthService : RPC {
    suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto
    suspend fun login(credentials: UserLoginDto): UserLoginResponseDto
}