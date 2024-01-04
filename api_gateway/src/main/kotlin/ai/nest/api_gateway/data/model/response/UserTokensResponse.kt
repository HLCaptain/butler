package ai.nest.api_gateway.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserTokensResponse(
    val accessTokenExpirationDate: Long,
    val refreshTokenExpirationDate: Long,
    val accessToken: String,
    val refreshToken: String
)