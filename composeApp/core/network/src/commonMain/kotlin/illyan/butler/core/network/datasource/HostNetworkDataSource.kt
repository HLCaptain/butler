package illyan.butler.core.network.datasource

interface HostNetworkDataSource {
    // TODO: make this return a string or DTO for a special message from LLM (cute hello message)
    suspend fun tryToConnect(url: String): Boolean
}