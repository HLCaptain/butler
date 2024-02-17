package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ModelDto
import kotlinx.coroutines.flow.Flow

interface AvailableModelsNetworkDataSource {
    fun fetch(): Flow<List<ModelDto>>
}