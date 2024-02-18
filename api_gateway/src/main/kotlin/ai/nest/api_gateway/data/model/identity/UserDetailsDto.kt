package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val fullName: String?,
    val phone: String?,
    val address: AddressDto?
)