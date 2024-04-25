package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.data.network.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface ResourceNetworkDataSource {
    fun fetchResource(resourceId: String): ResourceDto?
}