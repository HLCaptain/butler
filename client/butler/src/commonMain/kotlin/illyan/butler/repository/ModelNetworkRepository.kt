package illyan.butler.repository

import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import org.koin.core.annotation.Single

@Single
class ModelNetworkRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource
) : ModelRepository {
    override suspend fun getAvailableModels(): Map<ModelDto, List<String>> = modelNetworkDataSource.fetchAll()
}
