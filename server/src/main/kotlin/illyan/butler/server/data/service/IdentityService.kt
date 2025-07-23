package illyan.butler.server.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import illyan.butler.server.data.datasource.UserDataSource
import illyan.butler.server.data.db.UserDatabase
import illyan.butler.server.utils.Claim
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.authenticate.TokenConfiguration
import illyan.butler.shared.model.authenticate.TokenType
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Single
class IdentityService(
    private val userDatabase: UserDatabase
) : UserDataSource {
    suspend fun createUser(
        newUser: UserRegistrationDto,
        tokenConfiguration: TokenConfiguration
    ): UserLoginResponseDto {
        val user = registerUser(newUser.email, newUser.password)
        return UserLoginResponseDto(user, generateUserTokens(user.id!!, tokenConfiguration))
    }

    suspend fun loginUser(
        email: String,
        password: String,
        tokenConfiguration: TokenConfiguration,
    ): UserLoginResponseDto {
        val user = getUserByEmailAndPassword(email, password)
        return UserLoginResponseDto(user, generateUserTokens(user.id!!, tokenConfiguration))
    }

    fun generateUserTokens(
        userId: Uuid,
        tokenConfiguration: TokenConfiguration
    ) = UserTokensResponse(
        userId,
        (Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toEpochMilliseconds(),
        (Clock.System.now() + tokenConfiguration.refreshTokenExpireDuration).toEpochMilliseconds(),
        generateToken(userId, tokenConfiguration, TokenType.ACCESS_TOKEN),
        generateToken(userId, tokenConfiguration, TokenType.REFRESH_TOKEN)
    )

    private fun generateToken(
        userId: Uuid,
        tokenConfiguration: TokenConfiguration,
        tokenType: TokenType
    ) = JWT.create()
        .withIssuer(tokenConfiguration.issuer)
        .withAudience(tokenConfiguration.audience)
        .withExpiresAt((Clock.System.now() + tokenConfiguration.accessTokenExpireDuration).toJavaInstant())
        .withClaim(Claim.USER_ID, userId.toString())
        .withClaim(Claim.TOKEN_TYPE, tokenType.name)
        .sign(Algorithm.HMAC256(tokenConfiguration.secret))

    override suspend fun getUser(userId: Uuid): UserDto {
        return userDatabase.getUser(userId)
    }

    override suspend fun getUserByEmailAndPassword(email: String, password: String): UserDto {
        return userDatabase.getUserByEmailAndPassword(email, password)
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return userDatabase.createUser(user)
    }

    override suspend fun editUser(user: UserDto) {
        userDatabase.updateUser(user)
    }

    override suspend fun deleteUser(userId: Uuid) {
        userDatabase.deleteUser(userId)
    }

    override fun getUserChanges(userId: Uuid): Flow<UserDto> {
        TODO("Not yet implemented")
    }

    suspend fun registerUser(email: String, password: String): UserDto {
        return createUser(UserDto(Uuid.random(), email)).also {
            userDatabase.upsertPasswordForUser(it.id, password)
        }
    }
}