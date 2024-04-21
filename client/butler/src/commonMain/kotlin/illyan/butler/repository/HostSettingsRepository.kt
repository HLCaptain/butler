package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.network.datasource.HostNetworkDataSource
import illyan.butler.di.KoinNames
import illyan.butler.repository.HostRepository.Companion.KEY_API_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class HostSettingsRepository(
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val settings: FlowSettings,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : HostRepository {

    private val _isConnectingToHost = MutableStateFlow(false)
    override val isConnectingToHost = _isConnectingToHost.asStateFlow()

    override val currentHost = settings.getStringOrNullFlow(KEY_API_URL).stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        null
    )

    override suspend fun testAndSelectHost(url: String): Boolean {
        return testHost(url).also { isHostAvailable ->
            if (isHostAvailable) settings.putString(KEY_API_URL, url)
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
            hostNetworkDataSource.tryToConnect(url).also { testingEnded = true}
        } catch (e: Exception) {
            false
        }.also { _isConnectingToHost.update { false } }
    }
}