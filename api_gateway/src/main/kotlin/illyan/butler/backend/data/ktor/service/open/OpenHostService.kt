package illyan.butler.backend.data.ktor.service.open

import kotlinx.rpc.RPC

interface OpenHostService : RPC {
    suspend fun tryToConnect(url: String): Boolean
}