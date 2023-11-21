package illyan.butler.data.firestore.datasource

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import illyan.butler.data.firestore.model.FirestoreChat
import illyan.butler.data.network.ChatNetworkDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ChatFirestoreDataSource(
    private val firestore: FirebaseFirestore
) : ChatNetworkDataSource {
    override fun fetch(uuid: String): Flow<FirestoreChat> {
        return firestore.collection(FirestoreChat.COLLECTION_NAME)
            .document(uuid)
            .snapshots()
            .map { it.data(FirestoreChat.serializer()) }
            .catch { Napier.e("There was a problem, loading chat", it) }
    }

    override fun fetchByUser(userUUID: String): Flow<List<FirestoreChat>> {
        return firestore.collection(FirestoreChat.COLLECTION_NAME)
            .where(FirestoreChat.FIELD_USER_UUID, equalTo = userUUID)
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.data(FirestoreChat.serializer()) } }
            .catch { Napier.e("There was a problem, loading chats", it) }
    }

    override fun fetchByModel(modelUUID: String): Flow<List<FirestoreChat>> {
        return firestore.collection(FirestoreChat.COLLECTION_NAME)
            .where(FirestoreChat.FIELD_MODEL_UUID, equalTo = modelUUID)
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.data(FirestoreChat.serializer()) } }
            .catch { Napier.e("There was a problem, loading chats", it) }
    }

    override suspend fun upsert(chat: FirestoreChat) {
        firestore.collection(FirestoreChat.COLLECTION_NAME)
            .document(chat.uuid)
            .set(chat)
    }

    override suspend fun delete(uuid: String) {
        firestore.collection(FirestoreChat.COLLECTION_NAME)
            .document(uuid)
            .delete()
    }

    override suspend fun deleteForUser(userUUID: String) {
        firestore.collection(FirestoreChat.COLLECTION_NAME)
            .where(FirestoreChat.FIELD_USER_UUID, equalTo = userUUID)
            .get()
            .apply { documents.forEach { it.reference.delete() } }
    }
}