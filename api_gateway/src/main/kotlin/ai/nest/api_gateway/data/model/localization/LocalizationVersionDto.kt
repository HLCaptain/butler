package ai.nest.api_gateway.data.model.localization

import kotlinx.serialization.Serializable

@Serializable
data class LocalizationVersionDto(
    val version: Long, // Last updated at time
    val oldVersion: Long? = null,
    val changedLabelIds: List<Int>? = null
)
