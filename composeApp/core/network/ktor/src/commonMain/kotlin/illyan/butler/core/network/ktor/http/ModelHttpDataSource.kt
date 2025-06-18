package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ModelNetworkDataSource
import illyan.butler.core.network.ktor.http.di.KtorHttpClientFactory
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.DomainModel
import illyan.butler.shared.model.chat.Source
import illyan.butler.shared.model.llm.ModelDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single

@OptIn(ExperimentalSerializationApi::class)
@Single
class ModelHttpDataSource(
    private val clientFactory: KtorHttpClientFactory
) : ModelNetworkDataSource {
    override suspend fun fetchAll(source: Source.Server): List<DomainModel> {
        return try {
            clientFactory(source).get("/models").body<List<ModelDto>>().map { it.toDomainModel() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
