package illyan.butler.repository

import illyan.butler.data.network.ModelNetworkDataSource
import org.koin.core.annotation.Single

@Single
class ModelRepository(
    private val modelNetworkDataSource: ModelNetworkDataSource
) {
    fun getAvailableModels() = modelNetworkDataSource.fetchAll()
}
