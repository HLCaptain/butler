package ai.nest.api_gateway.data.model.localization

import kotlinx.serialization.Serializable

@Serializable
data class LocalizationVersionDto(
    val currentVersion: Long, // Last updated at time
)
