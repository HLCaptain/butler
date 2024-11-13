package illyan.butler.data.host

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Single

@Single
class HostMemoryRepository : HostRepository {

    private val _isConnectingToHost = MutableStateFlow(false)
    override val isConnectingToHost = _isConnectingToHost.asStateFlow()

    private val _currentHost = MutableStateFlow<String?>(null)
    override val currentHost = _currentHost.asStateFlow()

    override suspend fun testAndSelectHost(url: String): Boolean {
        return testHost(url).also { isHostAvailable ->
            if (isHostAvailable) _currentHost.update { url }
        }
    }

    override suspend fun testHost(url: String): Boolean {
        _isConnectingToHost.update { true }
        return try {
            withTimeout(5000) {
                // Simulate a network connection test
                delay(1000)
                true
            }
        } catch (e: TimeoutCancellationException) {
            false
        }.also { _isConnectingToHost.update { false } }
    }

    override suspend fun selectHostWithoutTest(url: String) {
        _currentHost.update { url }
    }
}