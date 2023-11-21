package illyan.butler.data.firestore.model

import kotlinx.serialization.Serializable

@Serializable
data class FirestoreModel(
    val name: String,
    val uuid: String,
    val type: String,
    val description: String,
    val greetingMessage: String,
    val author: String,
) {
    companion object {
        const val COLLECTION_NAME = "models"
    }
}
