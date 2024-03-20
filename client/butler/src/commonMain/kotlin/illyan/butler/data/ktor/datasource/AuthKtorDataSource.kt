package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.auth.UserTokensResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

@Single
class AuthKtorDataSource(
    private val client: HttpClient
) : AuthNetworkDataSource {
    override suspend fun signup(credentials: UserRegistrationDto): UserDetailsDto {
        return client.post("/signup") {
            setBody(credentials)
        }.body()
    }

    override suspend fun login(credentials: UserLoginDto): UserTokensResponse {
        return client.post("/login") {
            setBody(credentials)
        }.body()
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
        return client.post("/reset-password") {
            setBody(request)
        }.status.isSuccess()
    }

    override suspend fun getMe(): Flow<UserDetailsDto?> {
        return flow {
            client.webSocket("/me") {
                incoming.receiveAsFlow().collectLatest { emit(receiveDeserialized()) }
            }
        }
    }
}