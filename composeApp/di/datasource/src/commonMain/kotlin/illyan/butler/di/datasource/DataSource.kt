package illyan.butler.di.datasource

import illyan.butler.config.BuildConfig
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.core.network.datasource.HostNetworkDataSource
import illyan.butler.core.network.ktor.http.AuthHttpDataSource
import illyan.butler.core.network.ktor.http.HostHttpDataSource
import illyan.butler.shared.model.auth.PasswordResetRequest
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse
import kotlinx.coroutines.delay
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
fun provideHostNetworkDataSource(
    hostHttpDataSource: HostHttpDataSource
): HostNetworkDataSource = if (BuildConfig.USE_MEMORY_DB) {
    object : HostNetworkDataSource {
        override suspend fun tryToConnect(url: String): Boolean {
            delay(1000)
            return true
        }
    }
} else hostHttpDataSource

@OptIn(ExperimentalUuidApi::class)
@Single
fun provideAuthNetworkDataSource(
    authNetworkDataSource: AuthHttpDataSource
): AuthNetworkDataSource = if (BuildConfig.USE_MEMORY_DB) {
    object : AuthNetworkDataSource {
        val users = mutableMapOf<String, UserDto>()
        val userWithCredential = mutableMapOf<Pair<String, String>, String>()

        override suspend fun signup(credentials: UserRegistrationDto): UserLoginResponseDto {
            delay(1000)
            val userId = Uuid.random().toString()
            users[userId] = UserDto(
                id = userId,
                email = credentials.email,
            )
            userWithCredential[credentials.email to credentials.password] = userId
            return UserLoginResponseDto(
                user = users[userId]!!,
                tokensResponse = UserTokensResponse(
                    userId = userId,
                    accessToken = Uuid.random().toString(),
                    refreshToken = Uuid.random().toString(),
                    accessTokenExpirationMillis = 10.days.inWholeMilliseconds,
                    refreshTokenExpirationMillis = 30.days.inWholeMilliseconds
                )
            )
        }

        override suspend fun login(credentials: UserLoginDto): UserLoginResponseDto {
            delay(1000)
            val user = try {
                users[userWithCredential[credentials.email to credentials.password]]!!
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid credentials")
            }
            return UserLoginResponseDto(
                user = user,
                tokensResponse = UserTokensResponse(
                    userId = user.id!!,
                    accessToken = Uuid.random().toString(),
                    refreshToken = Uuid.random().toString(),
                    accessTokenExpirationMillis = 10.days.inWholeMilliseconds,
                    refreshTokenExpirationMillis = 30.days.inWholeMilliseconds
                )
            )
        }

        override suspend fun sendPasswordResetEmail(request: PasswordResetRequest): Boolean {
            delay(1000)
            return true
        }
    }
} else authNetworkDataSource
