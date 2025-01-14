package illyan.butler.core.network.datasource

import illyan.butler.domain.model.DomainModel

interface ModelNetworkDataSource {
    suspend fun fetchAll(): List<DomainModel>
}
