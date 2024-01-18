package ai.nest.api_gateway.data.model.authenticate

import kotlin.time.Duration

data class TokenConfiguration(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessTokenExpireDuration: Duration,
    val refreshTokenExpireDuration: Duration
) {
    val accessTokenExpireMilli: Long
        get() = accessTokenExpireDuration.inWholeMilliseconds
    val refreshTokenExpireMilli: Long
        get() = refreshTokenExpireDuration.inWholeMilliseconds
}