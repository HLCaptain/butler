package illyan.butler.data.model

import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto
import org.koin.core.annotation.Single

@Single
class ModelNetworkRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource
) : ModelRepository {
    override suspend fun getAvailableModels(): Map<DomainModel, List<String>> = modelNetworkDataSource.fetchAll()
}
