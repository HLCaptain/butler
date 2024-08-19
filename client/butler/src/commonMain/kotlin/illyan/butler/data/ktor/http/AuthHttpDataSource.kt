package illyan.butler.data.ktor.http

import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import org.koin.core.annotation.Single

@Single
class AuthHttpDataSource(
    private val client: HttpClient,
) : AuthNetworkDataSource {

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