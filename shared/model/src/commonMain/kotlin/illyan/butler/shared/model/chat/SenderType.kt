package illyan.butler.shared.model.chat

import kotlin.uuid.ExperimentalUuidApi

/**
 * Who sent this message.
 */
sealed interface SenderType {
    @OptIn(ExperimentalUuidApi::class)
    data class User(val source: Source) : SenderType // a user (human) sent this message
    data class Ai(val source: AiSource) : SenderType // an AI (machine) sent this message
}
