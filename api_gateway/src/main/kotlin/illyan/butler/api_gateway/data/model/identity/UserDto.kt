package illyan.butler.api_gateway.data.model.identity

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