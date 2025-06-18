package illyan.butler.shared.model.response

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class UserTokensResponse(
    val userId: Uuid,
    val accessTokenExpirationMillis: Long,
    val refreshTokenExpirationMillis: Long,
    val accessToken: String,
    val refreshToken: String
)
