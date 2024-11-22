package illyan.butler.shared.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    val email: String,
    val password: String,
    val userName: String
)