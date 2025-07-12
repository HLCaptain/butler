package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Model
import illyan.butler.shared.model.chat.Source

interface ModelNetworkDataSource {
    suspend fun fetchAll(source: Source.Server): List<Model>
}
