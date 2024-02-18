package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
)