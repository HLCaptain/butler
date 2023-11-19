package illyan.butler.data.firestore.model

import kotlinx.serialization.Serializable

@Serializable
data class FirestoreUser(
    val id: String,
    val ownedBrokers: List<String>,
    val ownedPlants: List<String>,
)
