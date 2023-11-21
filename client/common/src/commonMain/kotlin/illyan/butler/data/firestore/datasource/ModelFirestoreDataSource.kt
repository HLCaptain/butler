package illyan.butler.data.firestore.datasource

import dev.gitlive.firebase.firestore.FirebaseFirestore
import illyan.butler.data.firestore.model.FirestoreModel
import illyan.butler.data.network.ModelNetworkDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ModelFirestoreDataSource(
    private val firestore: FirebaseFirestore
) : ModelNetworkDataSource {
    override fun fetch(uuid: String): Flow<FirestoreModel> {
        return firestore.collection(FirestoreModel.COLLECTION_NAME)
            .document(uuid)
            .snapshots()
            .map { it.data(FirestoreModel.serializer()) }
            .catch { Napier.d("Error fetching model with uuid $uuid") }
    }

    override fun fetchAll(): Flow<List<FirestoreModel>> {
        return firestore.collection(FirestoreModel.COLLECTION_NAME)
            .snapshots()
            .map { it.documents.map { document -> document.data(FirestoreModel.serializer()) } }
            .catch { Napier.d("Error fetching all models") }
    }
}