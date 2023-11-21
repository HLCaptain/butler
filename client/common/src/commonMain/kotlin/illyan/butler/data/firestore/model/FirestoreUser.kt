package illyan.butler.data.firestore.model

import kotlinx.serialization.Serializable

@Serializable
data class FirestoreUser(
    val uuid: String,
    val favoriteModels: List<String>,
) {
    companion object {
        const val COLLECTION_NAME = "users"
    }
}
