package illyan.butler.backend.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String?,
    val email: String,
    val username: String,
    val displayName: String? = null,
    val phone: String? = null,
    val fullName: String? = null,
    val photoUrl: String? = null,
    val address: AddressDto? = null
)