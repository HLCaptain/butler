package illyan.butler.repository.host

import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class HostMemoryRepository(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : HostRepository {

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
            var testingEnded = false
            coroutineScope.launch {
                delay(5000)
                if (!testingEnded) throw Exception("Connection timeout")
            }
            // Simulate a network connection test
            delay(1000)
            testingEnded = true
            true
        } catch (e: Exception) {
            false
        }.also { _isConnectingToHost.update { false } }
    }
}