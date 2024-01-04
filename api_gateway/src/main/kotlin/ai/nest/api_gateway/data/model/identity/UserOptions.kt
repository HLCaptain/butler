package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class UserOptions(
    val page: Int?,
    val limit: Int?,
    val query: String?,
    val permissions: List<Int>?,
    val country: List<String>?
)
