package illyan.butler.shared.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDto(
    val email: String,
    val password: String
)
