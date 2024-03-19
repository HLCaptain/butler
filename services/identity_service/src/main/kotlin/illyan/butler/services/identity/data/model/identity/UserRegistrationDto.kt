package illyan.butler.services.identity.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    val email: String,
    val userName: String,
    val password: String
)
