package illyan.butler.core.local.sqldelight.mapping

import illyan.butler.core.local.sqldelight.Credentials
import illyan.butler.core.local.sqldelight.User_tokens
import illyan.butler.domain.model.Token
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Credentials.toDomainModel(): ApiKeyCredential {
    return ApiKeyCredential(
        providerUrl = providerUrl,
        apiKey = apiKey
    )
}

fun ApiKeyCredential.toSqlDelightModel(): SqlDelightCredential {
    return SqlDelightCredential(
        providerUrl = providerUrl,
        apiKey = apiKey
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun User_tokens.toDomainModel(): UserTokens {
    return UserTokens(
        accessToken = accessToken?.let { token ->
            Token(
                token = token,
                tokenExpiration = accessTokenExpirationTime!!.let { expirationTime ->
                    Instant.fromEpochMilliseconds(expirationTime)
                }
            )
        },
        refreshToken = refreshToken?.let { token ->
            Token(
                token = token,
                tokenExpiration = refreshTokenExpirationTime!!.let { expirationTime ->
                    Instant.fromEpochMilliseconds(expirationTime)
                }
            )
        }
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun UserTokens.toSqlDelightModel(userId: Uuid): SqlDelightUserTokens {
    return SqlDelightUserTokens(
        userId = userId.toString(),
        accessToken = accessToken?.token,
        refreshToken = refreshToken?.token,
        accessTokenExpirationTime = accessToken?.tokenExpiration?.toEpochMilliseconds(),
        refreshTokenExpirationTime = refreshToken?.tokenExpiration?.toEpochMilliseconds(),
    )
}

data class SqlDelightCredential(
    val providerUrl: String,
    val apiKey: String
)

data class SqlDelightUserTokens(
    val userId: String,
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpirationTime: Long?,
    val refreshTokenExpirationTime: Long?,
)
