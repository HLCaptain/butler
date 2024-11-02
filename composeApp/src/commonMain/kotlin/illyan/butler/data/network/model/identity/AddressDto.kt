package illyan.butler.data.network.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
)