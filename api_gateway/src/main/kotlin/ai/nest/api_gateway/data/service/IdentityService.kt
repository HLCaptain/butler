package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.authenticate.TokenConfiguration
import ai.nest.api_gateway.data.model.authenticate.TokenType
import ai.nest.api_gateway.data.model.identity.AddressDto
import ai.nest.api_gateway.data.model.identity.LocationDto
import ai.nest.api_gateway.data.model.identity.UserDetailsDto
import ai.nest.api_gateway.data.model.identity.UserDto
import ai.nest.api_gateway.data.model.identity.UserOptions
import ai.nest.api_gateway.data.model.identity.UserRegistrationDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.model.response.UserTokensResponse
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.AppConfig
import ai.nest.api_gateway.utils.Claim.PERMISSIONS
import ai.nest.api_gateway.utils.Claim.TOKEN_TYPE
import ai.nest.api_gateway.utils.Claim.USERNAME
import ai.nest.api_gateway.utils.Claim.USER_ID
import ai.nest.api_gateway.utils.Permission
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
        userName: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
        applicationId: String
    ): UserTokensResponse {
        client.tryToExecute<Boolean> {
            post("${AppConfig.Api.IDENTITY_API_URL}/user/login") {
                headers.append(HttpHeaders.UserAgent, applicationId)
                formData {
                    parameter("username", userName)
                    parameter("password", password)
                }
            }
        }
        val user = getUserByUsername(userName)
        return generateUserTokens(user.id, userName, user.permissions, tokenConfiguration)
    }

    suspend fun getUsers(options: UserOptions) = client.tryToExecute<PaginationResponse<UserDto>> {
        post("${AppConfig.Api.IDENTITY_API_URL}/dashboard/user") {
            setBody(options)
        }
    }

    suspend fun getLastRegisteredUsers(limit: Int) = client.tryToExecute<List<UserDto>> {
        get("${AppConfig.Api.IDENTITY_API_URL}/dashboard/user/last-register") {
            parameter("limit", limit)
        }
    }

    suspend fun getUserAddresses(userId: String) = client.tryToExecute<List<AddressDto>> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/$userId/address")
    }

    suspend fun getUserById(id: String) = client.tryToExecute<UserDetailsDto> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/$id")
    }

    suspend fun updateUserProfile(
        id: String,
        fullName: String?,
        phone: String?,
    ) = client.tryToExecute<UserDetailsDto> {
        val formData = formData {
            fullName?.let { append("fullName", it) }
            phone?.let { append("phone", it) }
        }
        put("${AppConfig.Api.IDENTITY_API_URL}/user/$id") { setBody(formData) }
    }

    suspend fun getUserByUsername(username: String?) = client.tryToExecute<UserDto> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/get-user") {
            parameter("username", username)
        }
    }

    suspend fun updateUserPermission(userId: String, permission: Set<Int>) = client.tryToExecute<UserDto> {
        put("${AppConfig.Api.IDENTITY_API_URL}/dashboard/user/$userId/permission") {
            setBody(permission)
        }
    }

    suspend fun deleteUser(userId: String) = client.tryToExecute<Boolean> {
        delete("${AppConfig.Api.IDENTITY_API_URL}/user/$userId")
    }

    suspend fun getFavoriteRestaurantsIds(userId: String) = client.tryToExecute<List<String>> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/$userId/favorite")
    }

    suspend fun addRestaurantToFavorite(userId: String, restaurantId: String) = client.tryToExecute<Boolean> {
        post("${AppConfig.Api.IDENTITY_API_URL}/user/$userId/favorite") {
            formData {
                parameter("restaurantId", restaurantId)
            }
        }
    }

    suspend fun deleteRestaurantFromFavorite(userId: String, restaurantId: String) = client.tryToExecute<Boolean> {
        delete("${AppConfig.Api.IDENTITY_API_URL}/user/$userId/favorite") {
            formData {
                parameter("restaurantId", restaurantId)
            }
        }
    }

    fun generateUserTokens(
        userId: String,
        username: String,
        userPermission: Set<Permission>,
        tokenConfiguration: TokenConfiguration
    ) = UserTokensResponse(
        Clock.System.now() + tokenConfiguration.accessTokenExpireDuration,
        Clock.System.now() + tokenConfiguration.refreshTokenExpireDuration,
        generateToken(userId, username, userPermission, tokenConfiguration, TokenType.ACCESS_TOKEN),
        generateToken(userId, username, userPermission, tokenConfiguration, TokenType.REFRESH_TOKEN)
    )
    private fun generateToken(
        userId: String,
        username: String,
        userPermission: Set<Permission>,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt((Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toJavaInstant())
        .withClaim(USER_ID, userId)
        .withClaim(PERMISSIONS, userPermission.toList())
        .withClaim(USERNAME, username)
        .withClaim(TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))

    suspend fun updateUserLocation(userId: String, location: LocationDto) = client.tryToExecute<AddressDto> {
        post("${AppConfig.Api.IDENTITY_API_URL}/user/$userId/address/location") {
            setBody(location)
        }
    }

    suspend fun isUserExistedInDb(userId: String?) = client.tryToExecute<Boolean> {
        get("${AppConfig.Api.IDENTITY_API_URL}/user/isExisted/$userId")
    }

    suspend fun clearIdentityDB() = client.tryToExecute<Boolean> {
        delete("${AppConfig.Api.IDENTITY_API_URL}/collection")
    }
}