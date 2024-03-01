package illyan.butler.data.network.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val uuid: String,
    val favoriteModels: List<String>
)
