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
import ai.nest.api_gateway.utils.APIs
import ai.nest.api_gateway.utils.Claim.PERMISSION
import ai.nest.api_gateway.utils.Claim.TOKEN_TYPE
import ai.nest.api_gateway.utils.Claim.USERNAME
import ai.nest.api_gateway.utils.Claim.USER_ID
import ai.nest.api_gateway.utils.Role
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import java.util.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.koin.core.annotation.Single

@Single
class IdentityService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val localizationService: LocalizationService
) {
    suspend fun createUser(
        newUser: UserRegistrationDto,
        locale: Locale
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("/user") {
            setBody(newUser)
        }
    }

    suspend fun loginUser(
        userName: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
        locale: Locale,
        applicationId: String
    ): UserTokensResponse {
        client.tryToExecute<Boolean>(
            api = APIs.IDENTITY_API,
            attributes = attributes,
            setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
        ) {
            post("/user/login") {
                headers.append(HttpHeaders.UserAgent, applicationId)
                formData {
                    parameter("username", userName)
                    parameter("password", password)
                }
            }
        }
        val user = getUserByUsername(username = userName, locale)
        return generateUserTokens(user.id, userName, user.permission.first { it == Role.END_USER }, tokenConfiguration)
    }

    suspend fun getUsers(
        options: UserOptions,
        locale: Locale
    ) = client.tryToExecute<PaginationResponse<UserDto>>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("/dashboard/user") {
            setBody(options)
        }
    }

    suspend fun getLastRegisteredUsers(limit: Int) = client.tryToExecute<List<UserDto>>(
        APIs.IDENTITY_API,
        attributes = attributes
    ) {
        get("/dashboard/user/last-register") {
            parameter("limit", limit)
        }
    }

    suspend fun getUserAddresses(
        userId: String,
        locale: Locale
    ) = client.tryToExecute<List<AddressDto>>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("/user/$userId/address")
    }

    suspend fun getUserById(
        id: String,
        locale: Locale
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("user/$id")
    }

    suspend fun updateUserProfile(
        id: String,
        fullName: String?,
        phone: String?,
        locale: Locale
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        val formData = FormDataContent(
            Parameters.build {
                fullName?.let { append("fullName", it) }
                phone?.let { append("phone", it) }
            }
        )
        put("/user/$id") { setBody(formData) }
    }

    suspend fun getUserByUsername(
        username: String?,
        locale: Locale
    ) = client.tryToExecute<UserDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("user/get-user") {
            parameter("username", username)
        }
    }

    suspend fun updateUserPermission(
        userId: String,
        permission: Set<Role>,
        locale: Locale
    ) = client.tryToExecute<UserDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        put("/dashboard/user/$userId/permission") {
            setBody(permission)
        }
    }

    suspend fun deleteUser(
        userId: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        delete("/user/$userId")
    }

    suspend fun getFavoriteRestaurantsIds(
        userId: String,
        locale: Locale
    ) = client.tryToExecute<List<String>>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("/user/$userId/favorite")
    }

    suspend fun addRestaurantToFavorite(
        userId: String,
        restaurantId: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("/user/$userId/favorite") {
            formData {
                parameter("restaurantId", restaurantId)
            }
        }
    }

    suspend fun deleteRestaurantFromFavorite(
        userId: String,
        restaurantId: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        delete("/user/$userId/favorite") {
            formData {
                parameter("restaurantId", restaurantId)
            }
        }
    }

    fun generateUserTokens(
        userId: String,
        username: String,
        userPermission: Role,
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
        userPermission: Role,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt((Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toJavaInstant())
        .withClaim(USER_ID, userId)
        .withClaim(PERMISSION, userPermission.toString())
        .withClaim(USERNAME, username)
        .withClaim(TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))

    suspend fun updateUserLocation(
        userId: String,
        location: LocationDto,
        locale: Locale
    ) = client.tryToExecute<AddressDto>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("/user/$userId/address/location") {
            setBody(location)
        }
    }

    suspend fun isUserExistedInDb(
        userId: String?,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("user/isExisted/$userId")
    }

    suspend fun clearIdentityDB() = client.tryToExecute<Boolean>(
        APIs.IDENTITY_API,
        attributes = attributes
    ) {
        delete("/collection")
    }
}