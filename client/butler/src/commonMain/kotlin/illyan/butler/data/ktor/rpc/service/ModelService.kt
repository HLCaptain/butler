package illyan.butler.data.ktor.rpc.service

import illyan.butler.data.network.model.ai.ModelDto
import kotlinx.rpc.RPC

interface ModelService : RPC {
    suspend fun fetch(modelId: String): Pair<ModelDto, List<String>>
    suspend fun fetchAll(): Map<ModelDto, List<String>>
    suspend fun fetchProviders(): List<String>
}