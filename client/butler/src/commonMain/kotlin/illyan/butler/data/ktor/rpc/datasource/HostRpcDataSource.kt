package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.HostService
import illyan.butler.data.network.datasource.HostNetworkDataSource
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class HostRpcDataSource(
    private val hostService: HostService,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : HostNetworkDataSource {
    override suspend fun tryToConnect(url: String): Boolean {
        return hostService.tryToConnect(url)
    }
}