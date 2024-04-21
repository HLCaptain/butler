package illyan.butler.repository

import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ai.ModelDto
import org.koin.core.annotation.Single

interface ModelRepository {
    suspend fun getAvailableModels(): List<ModelDto>
}
