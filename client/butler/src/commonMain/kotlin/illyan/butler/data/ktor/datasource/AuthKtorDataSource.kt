package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.network.model.identity.UserDetailsDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AuthKtorDataSource(
    private val client: HttpClient
) : AuthNetworkDataSource {
    override suspend fun signup(credentials: UserRegistrationDto): UserDetailsDto {
        return client.get("/signup") {
            setBody(credentials)
        }.body()
    }

    override suspend fun login(credentials: UserLoginDto): UserTokensResponse {
        return client.get("/login") {
            setBody(credentials)
        }.body()
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
        return client.get("/reset-password") {
            setBody(request)
        }.status.isSuccess()
    }

    override suspend fun getMe(): Flow<UserDetailsDto?> {
        val stateFlowOfMe = MutableStateFlow<UserDetailsDto?>(null)
        client.webSocket("/me") {
            val user = receiveDeserialized<UserDetailsDto?>()
            stateFlowOfMe.update { user }
        }
        return stateFlowOfMe
    }
}