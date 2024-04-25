package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.ResourceDto

interface ResourceNetworkDataSource {
    suspend fun fetchResource(resourceId: String): ResourceDto?
}