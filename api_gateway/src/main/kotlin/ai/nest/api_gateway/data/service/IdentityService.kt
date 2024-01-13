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
import ai.nest.api_gateway.data.utils.ErrorHandler
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.APIs
import ai.nest.api_gateway.utils.Claim.PERMISSION
import ai.nest.api_gateway.utils.Claim.TOKEN_TYPE
import ai.nest.api_gateway.utils.Claim.USERNAME
import ai.nest.api_gateway.utils.Claim.USER_ID
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
import io.ktor.http.Parameters
import io.ktor.util.Attributes
import io.ktor.utils.io.InternalAPI
import java.util.Date
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@Single
class IdentityService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val errorHandler: ErrorHandler
) {
    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun createUser(
        newUser: UserRegistrationDto,
        languageCode: String
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        post("/user") {
            body = ProtoBuf.encodeToByteArray(UserRegistrationDto.serializer(), newUser)
        }
    }

    suspend fun loginUser(
        userName: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
        languageCode: String,
        applicationId: String
    ): UserTokensResponse {
        client.tryToExecute<Boolean>(
            api = APIs.IDENTITY_API,
            attributes = attributes,
            setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
        ) {
            post("/user/login") {
                headers.append("Application-Id", applicationId)
                formData {
                    parameter("username", userName)
                    parameter("password", password)
                }
            }
        }
        val user = getUserByUsername(username = userName, languageCode)
        return generateUserTokens(user.id, userName, user.permission, tokenConfiguration)
    }

    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun getUsers(
        options: UserOptions,
        languageCode: String
    ) = client.tryToExecute<PaginationResponse<UserDto>>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        post("/dashboard/user") {
            body = ProtoBuf.encodeToByteArray(UserOptions.serializer(), options)
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
        languageCode: String
    ) = client.tryToExecute<List<AddressDto>>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        get("/user/$userId/address")
    }

    suspend fun getUserById(
        id: String,
        languageCode: String
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        get("user/$id")
    }

    @OptIn(InternalAPI::class)
    suspend fun updateUserProfile(
        id: String,
        fullName: String?,
        phone: String?,
        languageCode: String
    ) = client.tryToExecute<UserDetailsDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        val formData = FormDataContent(
            Parameters.build {
                fullName?.let { append("fullName", it) }
                phone?.let { append("phone", it) }
            }
        )
        put("/user/$id") { body = formData }
    }

    suspend fun getUserByUsername(
        username: String?,
        languageCode: String
    ) = client.tryToExecute<UserDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        get("user/get-user") {
            parameter("username", username)
        }
    }

    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun updateUserPermission(
        userId: String,
        permission: List<Int>,
        languageCode: String
    ) = client.tryToExecute<UserDto>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        put("/dashboard/user/$userId/permission") {
            body = ProtoBuf.encodeToByteArray(ListSerializer(Int.serializer()), permission)
        }
    }

    suspend fun deleteUser(
        userId: String,
        languageCode: String
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        delete("/user/$userId")
    }

    suspend fun getFavoriteRestaurantsIds(
        userId: String,
        languageCode: String
    ) = client.tryToExecute<List<String>>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        get("/user/$userId/favorite")
    }

    suspend fun addRestaurantToFavorite(
        userId: String,
        restaurantId: String,
        languageCode: String
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
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
        languageCode: String
    ) = client.tryToExecute<Boolean>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
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
        userPermission: Int,
        tokenConfiguration: TokenConfiguration
    ) = UserTokensResponse(
        getExpirationDate(tokenConfiguration.accessTokenExpirationTimestamp).time,
        getExpirationDate(tokenConfiguration.refreshTokenExpirationTimestamp).time,
        generateToken(userId, username, userPermission, tokenConfiguration, TokenType.ACCESS_TOKEN),
        generateToken(userId, username, userPermission, tokenConfiguration, TokenType.REFRESH_TOKEN)
    )

    private fun getExpirationDate(timestamp: Long) = Date(System.currentTimeMillis() + timestamp)

    private fun generateToken(
        userId: String,
        username: String,
        userPermission: Int,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt(Date(System.currentTimeMillis() + tokenConfiguration.accessTokenExpirationTimestamp))
        .withClaim(USER_ID, userId)
        .withClaim(PERMISSION, userPermission.toString())
        .withClaim(USERNAME, username)
        .withClaim(TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))

    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun updateUserLocation(
        userId: String,
        location: LocationDto,
        languageCode: String
    ) = client.tryToExecute<AddressDto>(
        api = APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        post("/user/$userId/address/location") {
            body = ProtoBuf.encodeToByteArray(LocationDto.serializer(), location)
        }
    }

    suspend fun isUserExistedInDb(
        userId: String?,
        languageCode: String
    ) = client.tryToExecute<Boolean>(
        APIs.IDENTITY_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
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