package illyan.butler.shared.model.chat

/**
 * Status of a message in a conversation (for UI feedback).
 */
enum class MessageStatus {
    PENDING,    // not yet sent/processed
    SENT,       // successfully sent to AI/backend
    RECEIVED,   // we got a reply (only relevant for USER->AI messages)
    ERROR       // something went wrong
}