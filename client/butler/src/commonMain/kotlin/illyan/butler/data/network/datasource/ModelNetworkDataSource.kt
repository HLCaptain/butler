package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ModelDto

interface ModelNetworkDataSource {
    suspend fun fetch(uuid: String): ModelDto
    suspend fun fetchAll(): List<ModelDto>
}