package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.shared.model.llm.ModelDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class ModelHttpDataSource(private val client: HttpClient) : ModelNetworkDataSource {
    override suspend fun fetch(modelId: String): Pair<ModelDto, List<String>> {
        return client.get("/models/$modelId").body()
    }

    override suspend fun fetchAll(): Map<ModelDto, List<String>> {
        return client.get("/models").body()
    }

    override suspend fun fetchProviders(): List<String> {
        return client.get("/providers").body()
    }
}