package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.network.datasource.HostNetworkDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class HostRepository(
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val settings: FlowSettings,
) {
    companion object {
        const val KEY_API_URL: String = "api_url"
    }

    private val _isConnectingToHost = MutableStateFlow(false)
    val isConnectingToHost = _isConnectingToHost.asStateFlow()

    private val _isConnectedToHost = MutableStateFlow<Boolean?>(null)
    val isConnectedToHost = _isConnectedToHost.asStateFlow()

    val currentHost = settings.getStringOrNullFlow(KEY_API_URL)

    suspend fun testAndSelectHost(url: String) {
        if (testHost(url)) settings.putString(KEY_API_URL, url)
    }

    suspend fun testHost(url: String): Boolean {
        _isConnectingToHost.update { true }
        _isConnectedToHost.update { null }
        return hostNetworkDataSource.tryToConnect(url).also { connected ->
            _isConnectedToHost.update { connected }
            _isConnectingToHost.update { false }
        }
    }
}