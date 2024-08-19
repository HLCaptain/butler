package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ResourceService
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Single

@Single
class ResourceRpcDataSource(
    private val resourceService: StateFlow<ResourceService?>,
) : ResourceNetworkDataSource {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchNewResources(): Flow<List<ResourceDto>> {
        return resourceService.flatMapLatest { service ->
            service?.fetchNewResources() ?: emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchResourceById(resourceId: String): Flow<ResourceDto> {
        return resourceService.flatMapLatest { service ->
            service?.fetchResourceById(resourceId) ?: emptyFlow()
        }
    }

    override suspend fun upsert(resource: ResourceDto): ResourceDto {
        return resourceService.value?.upsert(resource) ?: throw IllegalStateException("ResourceService is not available")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchByUser(): Flow<List<ResourceDto>> {
        return resourceService.flatMapLatest { service ->
            service?.fetchByUser() ?: emptyFlow()
        }
    }

    override suspend fun delete(resourceId: String): Boolean {
        return resourceService.value?.delete(resourceId) ?: throw IllegalStateException("ResourceService is not available")
    }
}
