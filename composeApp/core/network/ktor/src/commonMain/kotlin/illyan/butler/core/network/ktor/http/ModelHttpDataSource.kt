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
    override suspend fun fetchAll(): List<DomainModel> {
        return client.get("/models").body<List<ModelDto>>().map { it.toDomainModel() }
    }
}
