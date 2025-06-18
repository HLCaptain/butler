package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.core.network.ktor.http.di.KtorHttpClientFactory
import illyan.butler.core.network.ktor.http.di.KtorUnauthorizedHttpClientFactory
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.domain.model.User
import illyan.butler.shared.model.auth.PasswordResetRequest
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.chat.Source
import illyan.butler.shared.model.identity.UserDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
@Single
class AuthHttpDataSource(
    private val unauthorizedClientFactory: KtorUnauthorizedHttpClientFactory,
    private val clientFactory: KtorHttpClientFactory,
) : AuthNetworkDataSource {

    override suspend fun signup(credentials: UserRegistrationDto, endpoint: String): UserLoginResponseDto {
        return unauthorizedClientFactory(endpoint).post("/signup") {
            setBody(credentials)
        }.body()
    }

    override suspend fun login(credentials: UserLoginDto, endpoint: String): UserLoginResponseDto {
        return unauthorizedClientFactory(endpoint).post("/login") {
            setBody(credentials)
        }.body()
    }

    override suspend fun sendPasswordResetEmail(request: PasswordResetRequest, endpoint: String): Boolean {
        return unauthorizedClientFactory(endpoint).post("/reset-password") {
            setBody(request)
        }.status.isSuccess()
    }

    override fun getUser(source: Source.Server): Flow<User> = flow {
        emit(clientFactory(source).get("/me").body<UserDto>().toDomainModel(source.endpoint))
    }

    override suspend fun updateUserData(user: User): User {
        return clientFactory(Source.Server(user.id, user.endpoint)).post("/me") {
            setBody(user.toNetworkModel())
        }.body<UserDto>().toDomainModel(user.endpoint)
    }
}
