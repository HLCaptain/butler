package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class ModelKtorDataSource(
    private val client: HttpClient
) : ModelNetworkDataSource {
    override suspend fun fetch(modelId: String): ModelDto {
        return client.get("/models/$modelId").body()
    }

    override suspend fun fetchAll(): List<ModelDto> {
        return client.get("/models").body()
    }
}