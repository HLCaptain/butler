package illyan.butler.model

import kotlinx.serialization.Serializable

@Serializable
data class DomainToken(
    val token: String,
    val tokenExpirationMillis: Long
)
