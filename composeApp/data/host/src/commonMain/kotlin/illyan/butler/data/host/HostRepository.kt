package illyan.butler.data.host

import kotlinx.coroutines.flow.StateFlow

interface HostRepository {
    val isConnectingToHost: StateFlow<Boolean>
    val currentHost: StateFlow<String?>

    suspend fun testAndSelectHost(url: String): Boolean
    suspend fun testHost(url: String): Boolean
    suspend fun selectHostWithoutTest(url: String)
}