package illyan.butler.api_gateway.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import illyan.butler.api_gateway.data.model.authenticate.TokenConfiguration
import illyan.butler.api_gateway.data.model.authenticate.TokenType
import illyan.butler.api_gateway.data.model.authenticate.UserLoginResponseDto
import illyan.butler.api_gateway.data.model.identity.UserDto
import illyan.butler.api_gateway.data.model.identity.UserRegistrationDto
import illyan.butler.api_gateway.data.model.response.UserTokensResponse
import illyan.butler.api_gateway.data.utils.getOrPutWebSocketFlow
import illyan.butler.api_gateway.data.utils.tryToExecute
import illyan.butler.api_gateway.endpoints.utils.WebSocketSessionManager
import illyan.butler.api_gateway.utils.AppConfig
import illyan.butler.api_gateway.utils.Claim
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.koin.core.annotation.Single

@Single
class IdentityService(
    private val client: HttpClient,
    private val webSocketSessionManager: WebSocketSessionManager
) {
    suspend fun createUser(
        newUser: UserRegistrationDto,
        tokenConfiguration: TokenConfiguration
    ): UserLoginResponseDto {
        val user = client.tryToExecute<UserDto> {
            post("${AppConfig.Api.IDENTITY_API_URL}/users") { setBody(newUser) }
        }
        return UserLoginResponseDto(user, generateUserTokens(user.id, tokenConfiguration))
    }

    suspend fun loginUser(
        email: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
    ): UserLoginResponseDto {
        val user = client.tryToExecute<UserDto> {
            post("${AppConfig.Api.IDENTITY_API_URL}/users/login") {
                formData {
                    parameter("email", email)
                    parameter("password", password)
                }
            }
        }
        return UserLoginResponseDto(user, generateUserTokens(user.id, tokenConfiguration))
    }

    suspend fun getUserById(id: String) = client.tryToExecute<UserDto> {
        get("${AppConfig.Api.IDENTITY_API_URL}/users/$id")
    }

    private val userDataFlows = mutableMapOf<String, StateFlow<UserDto?>>()
    fun getUserChangesById(id: String) = flow {
        val url = "${AppConfig.Api.IDENTITY_API_URL}/users/$id/changes"
        userDataFlows.getOrPutWebSocketFlow(url) {
            webSocketSessionManager.createSession(url)
        }.let { emitAll(it) }
    }

    suspend fun updateUserProfile(user: UserDto) = client.tryToExecute<UserDto> {
        put("${AppConfig.Api.IDENTITY_API_URL}/users/${user.id}") { setBody(user) }
    }

    suspend fun deleteUser(userId: String) = client.delete("${AppConfig.Api.IDENTITY_API_URL}/users/$userId").status.isSuccess()

    fun generateUserTokens(
        userId: String,
        tokenConfiguration: TokenConfiguration
    ) = UserTokensResponse(
        userId,
        (Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toEpochMilliseconds(),
        (Clock.System.now() + tokenConfiguration.refreshTokenExpireDuration).toEpochMilliseconds(),
        generateToken(userId, tokenConfiguration, TokenType.ACCESS_TOKEN),
        generateToken(userId, tokenConfiguration, TokenType.REFRESH_TOKEN)
    )

    private fun generateToken(
        userId: String,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt((Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toJavaInstant())
        .withClaim(Claim.USER_ID, userId)
        .withClaim(Claim.TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))
}