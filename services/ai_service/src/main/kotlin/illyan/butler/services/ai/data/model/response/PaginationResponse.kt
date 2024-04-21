package illyan.butler.services.ai.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginationResponse<T>(
    val items: List<T>,
    val offset: Int,
    val total: Long
)