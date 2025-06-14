package illyan.butler.domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class User(
    val id: Uuid,
    val endpoint: String, // e.g. "https://api.example.com/"
    val email: String,
    val username: String?,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: Address?,
    val accessToken: Token?,
    val refreshToken: Token?
)
