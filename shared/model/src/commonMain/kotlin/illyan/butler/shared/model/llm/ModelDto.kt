package illyan.butler.shared.model.llm

import kotlinx.serialization.Serializable

@Serializable
data class ModelDto(
    val name: String?,
    val id: String,
    val endpoint: String,
    val ownedBy: String?
)
