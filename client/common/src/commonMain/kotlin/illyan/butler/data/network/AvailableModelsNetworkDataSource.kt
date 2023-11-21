package illyan.butler.data.network

import illyan.butler.data.firestore.model.FirestoreModel
import kotlinx.coroutines.flow.Flow

interface AvailableModelsNetworkDataSource {
    fun fetch(): Flow<List<FirestoreModel>>
}