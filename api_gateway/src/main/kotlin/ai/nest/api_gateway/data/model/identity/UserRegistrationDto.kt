package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationDto(
    val userName: String,
    val password: String,
    val email: String
)