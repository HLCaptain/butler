package illyan.butler.shared.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserTokensResponse(
    val userId: String,
    val accessTokenExpirationMillis: Long,
    val refreshTokenExpirationMillis: Long,
    val accessToken: String,
    val refreshToken: String
)
