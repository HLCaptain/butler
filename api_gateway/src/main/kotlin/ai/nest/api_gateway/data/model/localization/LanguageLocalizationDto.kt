package ai.nest.api_gateway.data.model.localization

import kotlinx.serialization.Serializable

@Serializable
data class LanguageLocalizationDto(
    val languageCode: String,
    val labels: Set<LabelDto>,
    val version: Long
)
