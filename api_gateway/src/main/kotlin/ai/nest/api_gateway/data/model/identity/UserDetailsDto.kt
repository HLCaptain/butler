package ai.nest.api_gateway.data.model.identity

import ai.nest.api_gateway.utils.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsDto(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val walletBalance: Double,
    val currency: String,
    val addresses: List<AddressDto> = emptyList(),
    val country: String,
    val permission: Set<Role> = emptySet()
)