package illyan.butler.api_gateway.data.model.response

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserTokensResponse(
    val accessTokenExpirationDate: Instant,
    val refreshTokenExpirationDate: Instant,
    val accessToken: String,
    val refreshToken: String
)