package illyan.butler.data.firestore.datasource

import dev.gitlive.firebase.firestore.FirebaseFirestore
import illyan.butler.data.firestore.model.FirestoreModel
import illyan.butler.data.network.ModelNetworkDataSource
import kotlinx.coroutines.flow.Flow
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
    }
}