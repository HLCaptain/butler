package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val fullName: String,
    val username: String,
    val country: String,
    val phone: String,
    val email: String,
    val permission: Set<Int>
)