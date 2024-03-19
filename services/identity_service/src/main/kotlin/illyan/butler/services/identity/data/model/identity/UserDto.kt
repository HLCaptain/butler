package illyan.butler.services.identity.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val phone: String?
)