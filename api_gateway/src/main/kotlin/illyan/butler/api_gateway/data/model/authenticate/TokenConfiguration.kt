package illyan.butler.api_gateway.data.model.authenticate

import kotlin.time.Duration

data class TokenConfiguration(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessTokenExpireDuration: Duration,
    val refreshTokenExpireDuration: Duration
) {
    val accessTokenExpireMilli = accessTokenExpireDuration.inWholeMilliseconds
    val refreshTokenExpireMilli = refreshTokenExpireDuration.inWholeMilliseconds
}