package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Token
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.response.UserTokensResponse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun UserTokensResponse.toDomainModel() = UserTokens(
    accessToken = Token(
        token = accessToken,
        tokenExpiration = Instant.fromEpochMilliseconds(accessTokenExpirationMillis)
    ),
    refreshToken = Token(
        token = refreshToken,
        tokenExpiration = Instant.fromEpochMilliseconds(refreshTokenExpirationMillis)
    )
)
