package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

/**
 * Who sent this message.
 */
@Serializable
sealed interface SenderType {
    @OptIn(ExperimentalUuidApi::class)
    @Serializable
    data class User(val source: Source) : SenderType // a user (human) sent this message
    @Serializable
    data class Ai(val source: AiSource) : SenderType // an AI (machine) sent this message
    @Serializable
    data object System : SenderType // the system sent this message, e.g. a notification or an error message
}
