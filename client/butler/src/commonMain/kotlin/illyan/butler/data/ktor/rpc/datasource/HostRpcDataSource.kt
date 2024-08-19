package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.HostService
import illyan.butler.data.network.datasource.HostNetworkDataSource
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@Single
class HostRpcDataSource(
    private val hostService: StateFlow<HostService?>,
) : HostNetworkDataSource {
    override suspend fun tryToConnect(url: String): Boolean {
        return hostService.value?.tryToConnect(url) ?: throw IllegalStateException("HostService is not available")
    }
}