package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ModelDto

interface ModelNetworkDataSource {
    suspend fun fetchModel(uuid: String): ModelDto
    suspend fun fetchAllModels(): List<ModelDto>
}