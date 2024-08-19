package illyan.butler.data.ktor.rpc.service

import kotlinx.rpc.RPC

interface HostService : RPC {
    suspend fun tryToConnect(url: String): Boolean
}