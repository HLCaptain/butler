package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.network.datasource.HostNetworkDataSource
import illyan.butler.di.NamedCoroutineDispatcherIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class HostRepository(
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val settings: FlowSettings,
    @NamedCoroutineDispatcherIO private val coroutineScope: CoroutineScope
) {
    companion object {
        const val KEY_API_URL: String = "api_url"
    }

    private val _isConnectingToHost = MutableStateFlow(false)
    val isConnectingToHost = _isConnectingToHost.asStateFlow()

    val currentHost = settings.getStringOrNullFlow(KEY_API_URL).stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        null
    )

    suspend fun testAndSelectHost(url: String): Boolean {
        return testHost(url).also { isHostAvailable ->
            if (isHostAvailable) settings.putString(KEY_API_URL, url)
        }
    }

    suspend fun testHost(url: String): Boolean {
        _isConnectingToHost.update { true }
        return hostNetworkDataSource.tryToConnect(url).also { _ ->
            _isConnectingToHost.update { false }
        }
    }
}