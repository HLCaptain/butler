package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val username: String,
    val phone: String?
)