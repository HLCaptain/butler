package illyan.butler.data.network

import illyan.butler.data.firestore.model.FirestoreChat
import kotlinx.coroutines.flow.Flow

interface ChatNetworkDataSource {
    fun fetch(uuid: String): Flow<FirestoreChat>
    fun fetchByUser(userUUID: String): Flow<List<FirestoreChat>>
    fun fetchByModel(modelUUID: String): Flow<List<FirestoreChat>>
    suspend fun upsert(chat: FirestoreChat)
    suspend fun delete(uuid: String)
    suspend fun deleteForUser(userUUID: String)
}