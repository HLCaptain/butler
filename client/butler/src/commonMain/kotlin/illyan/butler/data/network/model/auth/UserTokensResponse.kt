package illyan.butler.data.network.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserTokensResponse(
    val accessTokenExpirationMillis: Long,
    val refreshTokenExpirationMillis: Long,
    val accessToken: String,
    val refreshToken: String
)