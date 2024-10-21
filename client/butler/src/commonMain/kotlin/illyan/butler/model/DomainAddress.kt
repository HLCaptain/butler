package illyan.butler.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainAddress(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
)