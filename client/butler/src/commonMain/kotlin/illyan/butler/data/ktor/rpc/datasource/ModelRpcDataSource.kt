package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ModelService
import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
class ModelRpcDataSource(
    private val modelService: StateFlow<ModelService?>,
) : ModelNetworkDataSource {
    override suspend fun fetch(modelId: String): Pair<ModelDto, List<String>> {
        return modelService.value?.fetch(modelId) ?: throw IllegalStateException("ModelService is not available")
    }

    override suspend fun fetchAll(): Map<ModelDto, List<String>> {
        return modelService.value?.fetchAll() ?: throw IllegalStateException("ModelService is not available")
    }

    override suspend fun fetchProviders(): List<String> {
        return modelService.value?.fetchProviders() ?: throw IllegalStateException("ModelService is not available")
    }
}