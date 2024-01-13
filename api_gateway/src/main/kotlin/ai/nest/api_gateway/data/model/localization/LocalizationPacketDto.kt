package ai.nest.api_gateway.data.model.localization

import kotlinx.serialization.Serializable

@Serializable
data class LocalizationPacketDto(
    val language: String,
    val labels: List<LabelDto>? = null,
) {
    val keys by lazy { labels?.map { it.id } ?: emptyList() }
}
