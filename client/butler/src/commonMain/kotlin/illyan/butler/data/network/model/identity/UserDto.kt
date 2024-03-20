package illyan.butler.data.network.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val address: AddressDto?,
    val photoUrl: String?
)
