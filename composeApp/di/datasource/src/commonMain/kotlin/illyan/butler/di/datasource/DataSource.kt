package illyan.butler.di.datasource

import illyan.butler.config.BuildConfig
import illyan.butler.core.network.datasource.AuthNetworkDataSource
import illyan.butler.core.network.datasource.HostNetworkDataSource
import illyan.butler.core.network.ktor.http.AuthHttpDataSource
import illyan.butler.core.network.ktor.http.HostHttpDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.domain.model.User
import illyan.butler.shared.model.auth.PasswordResetRequest
import illyan.butler.shared.model.auth.UserLoginDto
import illyan.butler.shared.model.auth.UserLoginResponseDto
import illyan.butler.shared.model.auth.UserRegistrationDto
import illyan.butler.shared.model.chat.FilterOption
import illyan.butler.shared.model.chat.Source
import illyan.butler.shared.model.identity.UserDto
import illyan.butler.shared.model.response.UserTokensResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val users = mutableMapOf<Uuid, UserDto>()
        val userWithCredential = mutableMapOf<Pair<String, String>, Uuid>()

        override suspend fun signup(credentials: UserRegistrationDto, endpoint: String): UserLoginResponseDto {
            delay(1000)
            val userId = Uuid.random()
            users[userId] = UserDto(
                id = userId,
                email = credentials.email,
                filters = setOf(FilterOption.FreeRegexFilter)
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

        override suspend fun login(credentials: UserLoginDto, endpoint: String): UserLoginResponseDto {
            delay(1000)
            val user = try {
                users[userWithCredential[credentials.email to credentials.password]]!!
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid credentials")
            }
            return UserLoginResponseDto(
                user = user,
                tokensResponse = UserTokensResponse(
                    userId = user.id,
                    accessToken = Uuid.random().toString(),
                    refreshToken = Uuid.random().toString(),
                    accessTokenExpirationMillis = 10.days.inWholeMilliseconds,
                    refreshTokenExpirationMillis = 30.days.inWholeMilliseconds
                )
            )
        }

        override suspend fun sendPasswordResetEmail(request: PasswordResetRequest, endpoint: String): Boolean {
            delay(1000)
            return true
        }

        override fun getUser(source: Source.Server): Flow<User> {
            return flow {
                delay(1000)
                val user = users[source.userId] ?: throw IllegalArgumentException("User not found")
                emit(user.toDomainModel(source.endpoint))
            }
        }

        override suspend fun updateUserData(user: User): User {
            delay(1000)
            val userId = user.id
            if (users.containsKey(userId)) {
                users[userId] = user.toNetworkModel()
                return users[userId]!!.toDomainModel(user.endpoint)
            } else {
                throw IllegalArgumentException("User not found")
            }
        }
    }
} else authNetworkDataSource
