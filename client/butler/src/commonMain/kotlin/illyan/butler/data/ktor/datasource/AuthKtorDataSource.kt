package illyan.butler.data.ktor.datasource

import illyan.butler.data.ktor.utils.WebSocketSessionManager
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.identity.UserDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AuthKtorDataSource(
    private val client: HttpClient,
    private val webSocketSessionManager: WebSocketSessionManager
) : AuthNetworkDataSource {
    private val newMeStateFlow = MutableStateFlow<UserDto?>(null)
    private var isLoadingMeWebSocketSession = false
    private var isLoadedMeWebSocketSession = false

    private suspend fun createNewMeFlow() {
        Napier.v { "Receiving new me" }
        val session = webSocketSessionManager.createSession("/me")
        session.incoming.receiveAsFlow().collectLatest {
            Napier.v { "Received new me" }
            newMeStateFlow.update { session.receiveDeserialized() }
        }
    }

    override suspend fun getMe(): Flow<UserDto?> {
        return if (newMeStateFlow.value == null && !isLoadingMeWebSocketSession && !isLoadedMeWebSocketSession) {
            isLoadingMeWebSocketSession = true
            flow {
                createNewMeFlow()
                isLoadedMeWebSocketSession = true
                isLoadingMeWebSocketSession = false
                emitAll(newMeStateFlow)
            }
        } else {
            newMeStateFlow
        }
    }

    override suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto {
        return client.post("/signup") {
            setBody(credentials)
        }.body()
    }

    override suspend fun login(credentials: UserLoginDto): UserLoginResponseDto {
        return client.post("/login") {
            setBody(credentials)
        }.body()
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
        return client.post("/reset-password") {
            setBody(request)
        }.status.isSuccess()
    }
}