package illyan.butler.domain.model

data class UserTokens(
    val accessToken: Token?,
    val refreshToken: Token?,
)
