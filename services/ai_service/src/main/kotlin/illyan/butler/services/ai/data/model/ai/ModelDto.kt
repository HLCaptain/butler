package illyan.butler.services.ai.data.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class ModelDto(
    val name: String?,
    val id: String,
    val description: String?,
    val greetingMessage: String?,
    val author: String?
)
