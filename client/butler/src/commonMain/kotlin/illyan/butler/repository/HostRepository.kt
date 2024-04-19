package illyan.butler.repository

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

interface HostRepository {
    companion object {
        const val KEY_API_URL: String = "api_url"
    }

    val isConnectingToHost: StateFlow<Boolean>
    val currentHost: StateFlow<String?>

    suspend fun testAndSelectHost(url: String): Boolean
    suspend fun testHost(url: String): Boolean
}