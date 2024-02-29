package illyan.butler.data.network.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    val email: String,
    val userName: String,
    val password: String
)