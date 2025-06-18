package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable

@Serializable
sealed interface FilterOption {
    @Serializable
    data class RegexFilter(
        val pattern: String,
    ) : FilterOption

    companion object {
        val FreeRegexFilter = RegexFilter(
            pattern = "free",
        )
    }
}
