package illyan.butler.data.network

import illyan.butler.data.firestore.model.FirestoreModel
import kotlinx.coroutines.flow.Flow

interface ModelNetworkDataSource {
    fun fetch(uuid: String): Flow<FirestoreModel>
    fun fetchAll(): Flow<List<FirestoreModel>>
}