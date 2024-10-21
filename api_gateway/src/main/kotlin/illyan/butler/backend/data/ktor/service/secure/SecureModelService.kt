package illyan.butler.backend.data.ktor.service.secure

import illyan.butler.backend.data.model.ai.ModelDto

interface SecureModelService : RPCWithJWT {
    suspend fun fetch(modelId: String): Pair<ModelDto, List<String>>
    suspend fun fetchAll(): Map<ModelDto, List<String>>
    suspend fun fetchProviders(): List<String>
}