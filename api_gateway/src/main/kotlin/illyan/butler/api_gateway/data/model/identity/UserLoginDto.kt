package illyan.butler.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDto(
    val email: String,
    val password: String
)
