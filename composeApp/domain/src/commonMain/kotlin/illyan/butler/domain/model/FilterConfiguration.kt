package illyan.butler.domain.model

import illyan.butler.shared.model.chat.FilterOption
import kotlinx.serialization.Serializable

@Serializable
data class FilterConfiguration(
    val selectedFilterType: FilterType,
    val filterOptions: Map<FilterOption, Boolean>
) {
    companion object {
        val Default = FilterConfiguration(
            selectedFilterType = FilterType.COMPANIES,
            filterOptions = mapOf(FilterOption.FreeRegexFilter to true)
        )
    }
}

enum class FilterType {
    COMPANIES,
    MODEL_ID
}
