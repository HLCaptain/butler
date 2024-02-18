package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.data.model.authenticate.TokenType
import ai.nest.api_gateway.data.model.identity.UserDetailsDto
import ai.nest.api_gateway.data.model.identity.UserDto
import ai.nest.api_gateway.data.model.identity.UserRegistrationDto
import ai.nest.api_gateway.data.model.response.UserTokensResponse
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.AppConfig
import ai.nest.api_gateway.utils.Claim.TOKEN_TYPE
import ai.nest.api_gateway.utils.Claim.USERNAME
import ai.nest.api_gateway.utils.Claim.USER_ID
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.koin.core.annotation.Single

@Single
class IdentityService(
    private val client: HttpClient,
) {
    suspend fun createUser(newUser: UserRegistrationDto) = client.tryToExecute<UserDetailsDto> {
        post("${AppConfig.Api.IDENTITY_API_URL}/user") {
            setBody(newUser)
        }
    }

    suspend fun loginUser(
        email: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
        applicationId: String
    ): UserTokensResponse {
        client.tryToExecute<Boolean> {
            post("${AppConfig.Api.IDENTITY_API_URL}/user/login") {
                headers.append(HttpHeaders.UserAgent, applicationId)
                formData {
                    parameter("email", email)
                    parameter("password", password)
                }
            }
        }
        val user = getUserByEmail(email)
        return generateUserTokens(user.id, email, tokenConfiguration)
    }

    suspend fun getUserById(id: String) = client.tryToExecute<UserDetailsDto> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/$id")
    }

    suspend fun updateUserProfile(
        id: String,
        name: String?,
        phone: String?
    ) = client.tryToExecute<UserDetailsDto> {
        val formData = formData {
            name?.let { append("name", it) }
            phone?.let { append("phone", it) }
        }
        put("${AppConfig.Api.IDENTITY_API_URL}/user/$id") { setBody(formData) }
    }

    suspend fun getUserByEmail(email: String) = client.tryToExecute<UserDto> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/get-user") {
            parameter("email", email)
        }
    }

    suspend fun deleteUser(userId: String) = client.tryToExecute<Boolean> {
        delete("${AppConfig.Api.IDENTITY_API_URL}/user/$userId")
    }

    fun generateUserTokens(
        userId: String,
        username: String,
        tokenConfiguration: TokenConfiguration
    ) = UserTokensResponse(
        Clock.System.now() + tokenConfiguration.accessTokenExpireDuration,
        Clock.System.now() + tokenConfiguration.refreshTokenExpireDuration,
        generateToken(userId, username, tokenConfiguration, TokenType.ACCESS_TOKEN),
        generateToken(userId, username, tokenConfiguration, TokenType.REFRESH_TOKEN)
    )
    private fun generateToken(
        userId: String,
        username: String,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt((Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toJavaInstant())
        .withClaim(USER_ID, userId)
        .withClaim(USERNAME, username)
        .withClaim(TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))
}