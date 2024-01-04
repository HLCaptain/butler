package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val id: String? = null,
    val location: LocationDto? = null,
    val address: String
)