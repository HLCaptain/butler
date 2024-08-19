package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ResourceService
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ResourceRpcDataSource(
    private val resourceService: ResourceService,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : ResourceNetworkDataSource {
    override fun fetchNewResources(): Flow<List<ResourceDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(resourceService.fetchNewResources())
            }
        }
    }

    override fun fetchResourceById(resourceId: String): Flow<ResourceDto> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(resourceService.fetchResourceById(resourceId))
            }
        }
    }

    override suspend fun upsert(resource: ResourceDto): ResourceDto {
        return resourceService.upsert(resource)
    }

    override fun fetchByUser(): Flow<List<ResourceDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(resourceService.fetchByUser())
            }
        }
    }

    override suspend fun delete(resourceId: String): Boolean {
        return resourceService.delete(resourceId)
    }
}