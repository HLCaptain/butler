package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.llm.ModelDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single

@Single
class ModelHttpDataSource(private val client: HttpClient) : ModelNetworkDataSource {
    override suspend fun fetch(modelId: String): Pair<DomainModel, List<String>> {
        return client.get("/models/$modelId").body<Pair<ModelDto, List<String>>>().let { it.first.toDomainModel() to it.second }
    }

    override suspend fun fetchAll(): Map<DomainModel, List<String>> {
        return client.get("/models").body<Map<ModelDto, List<String>>>().mapKeys { it.key.toDomainModel() }
    }

    override suspend fun fetchProviders(): List<String> {
        return client.get("/providers").body()
    }
}