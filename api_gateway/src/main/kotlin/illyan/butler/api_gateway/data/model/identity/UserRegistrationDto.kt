package illyan.butler.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    val email: String,
    val password: String,
    val userName: String
)
