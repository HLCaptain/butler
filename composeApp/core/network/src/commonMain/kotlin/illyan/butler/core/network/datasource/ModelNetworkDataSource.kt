package illyan.butler.core.network.datasource

import illyan.butler.shared.model.llm.ModelDto

interface ModelNetworkDataSource {
    suspend fun fetch(modelId: String): Pair<ModelDto, List<String>> // Model and available providers

    /**
     * Fetches all available models.
     * TODO: make this paginated.
     * @return list of available models.
     */
    suspend fun fetchAll(): Map<ModelDto, List<String>>

    suspend fun fetchProviders(): List<String>

    // TODO: make endpoints to get:
    //  - Top models
    //  - Recommended models based on user preferences
    //  - Searched up models
}