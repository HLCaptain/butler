package illyan.butler.data.host

import illyan.butler.core.local.room.dao.AppSettingsDao
import illyan.butler.core.network.datasource.HostNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Single

@Single
class HostRoomRepository(
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val appSettingsDao: AppSettingsDao,
) : HostRepository {
    private val _isConnectingToHost = MutableStateFlow(false)
    override val isConnectingToHost = _isConnectingToHost.asStateFlow()
    override val currentHost = appSettingsDao.getAppSettings().map { it?.hostUrl }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.Eagerly,
        initialValue = null
    )

    override suspend fun testAndSelectHost(url: String): Boolean {
        return testHost(url).also { isHostAvailable ->
            if (isHostAvailable) appSettingsDao.updateHostUrl(url)
        }
    }

    override suspend fun testHost(url: String): Boolean {
        if (url.isBlank()) return false
        _isConnectingToHost.update { true }
        return try {
            withTimeout(5000) {
                hostNetworkDataSource.tryToConnect(url)
            }
        } catch (e: Exception) {
            false
        }.also { _isConnectingToHost.update { false } }
    }

    override suspend fun selectHostWithoutTest(url: String) {
        appSettingsDao.updateHostUrl(url)
    }
}