package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ModelService
import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ModelRpcDataSource(
    private val modelService: ModelService,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : ModelNetworkDataSource {
    override suspend fun fetch(modelId: String): Pair<ModelDto, List<String>> {
        return modelService.fetch(modelId)
    }

    override suspend fun fetchAll(): Map<ModelDto, List<String>> {
        return modelService.fetchAll()
    }

    override suspend fun fetchProviders(): List<String> {
        return modelService.fetchProviders()
    }
}