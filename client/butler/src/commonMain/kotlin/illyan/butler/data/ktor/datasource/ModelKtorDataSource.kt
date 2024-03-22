package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ModelDto
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single
class ModelKtorDataSource(
    private val client: HttpClient
) : ModelNetworkDataSource {
    override suspend fun fetch(uuid: String): ModelDto {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAll(): List<ModelDto> {
        TODO("Not yet implemented")
    }
}