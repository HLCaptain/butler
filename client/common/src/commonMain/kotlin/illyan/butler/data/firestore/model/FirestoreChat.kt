package illyan.butler.data.firestore.model

import illyan.butler.domain.model.ChatMessage
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreChat(
    val uuid: String,
    val name: String?,
    val userUUID: String,
    val modelUUID: String,
    val messages: List<ChatMessage>,
) {
    companion object {
        const val COLLECTION_NAME = "chats"
        const val FIELD_USER_UUID = "userUUID"
        const val FIELD_MODEL_UUID = "modelUUID"
    }
}
