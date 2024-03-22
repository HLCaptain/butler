package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ModelDto

interface ModelNetworkDataSource {
    suspend fun fetch(uuid: String): ModelDto

    /**
     * Fetches all available models.
     * TODO: make this paginated.
     * @return list of available models.
     */
    suspend fun fetchAll(): List<ModelDto>

    // TODO: make endpoints to get:
    //  - Top models
    //  - Recommended models based on user preferences
    //  - Searched up models
}