package illyan.butler.ui.new_chat

import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.shared.model.chat.AiSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class NewChatState(
    val aiSources: List<AiSource>? = null,
    val creatingChat: Boolean = false,
    val filterConfiguration: FilterConfiguration = FilterConfiguration.Default,
    val newChatId: Uuid? = null,
)
