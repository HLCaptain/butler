package ai.nest.api_gateway.data.model.identity

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
    val permission: Int = 1
)