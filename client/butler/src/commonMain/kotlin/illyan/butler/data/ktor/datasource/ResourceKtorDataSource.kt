package illyan.butler.data.ktor.datasource

import illyan.butler.data.ktor.utils.WebSocketSessionManager
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.di.KoinNames
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ResourceKtorDataSource(
    private val client: HttpClient,
    private val webSocketSessionManager: WebSocketSessionManager,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : ResourceNetworkDataSource {
    override suspend fun fetchResource(resourceId: String): ResourceDto? {
        return client.get("/resources/$resourceId").body<ResourceDto?>()
    }
}