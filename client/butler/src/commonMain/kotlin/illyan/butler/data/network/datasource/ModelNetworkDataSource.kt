package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ModelDto
import kotlinx.coroutines.flow.Flow

interface ModelNetworkDataSource {
    fun fetch(uuid: String): Flow<ModelDto>
    fun fetchAll(): Flow<List<ModelDto>>
}