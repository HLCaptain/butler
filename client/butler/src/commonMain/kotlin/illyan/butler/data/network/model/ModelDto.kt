package illyan.butler.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelDto(
    val name: String,
    val uuid: String,
    val type: String,
    val description: String,
    val greetingMessage: String,
    val author: String
)
