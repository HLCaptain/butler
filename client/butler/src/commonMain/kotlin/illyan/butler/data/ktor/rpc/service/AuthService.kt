package illyan.butler.data.ktor.rpc.service

import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import kotlinx.rpc.RPC

interface AuthService : RPC {
    suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto
    suspend fun login(credentials: UserLoginDto): UserLoginResponseDto
    suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean
}