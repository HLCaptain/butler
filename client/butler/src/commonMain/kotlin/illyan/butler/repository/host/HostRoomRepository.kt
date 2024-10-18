package illyan.butler.repository.host

import illyan.butler.data.network.datasource.HostNetworkDataSource
import illyan.butler.data.local.room.dao.AppSettingsDao
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class HostRoomRepository(
    private val hostNetworkDataSource: HostNetworkDataSource,
    private val appSettingsDao: AppSettingsDao,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : HostRepository {
    private val _isConnectingToHost = MutableStateFlow(false)
    override val isConnectingToHost = _isConnectingToHost.asStateFlow()
    override val currentHost = appSettingsDao.getAppSettings().map { it?.hostUrl }.stateIn(
        coroutineScope,
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
            val connectionTimeoutJob = coroutineScope.launch {
                delay(5000)
                throw Exception("Connection timeout")
            }
            hostNetworkDataSource.tryToConnect(url).also { connectionTimeoutJob.cancel() }
        } catch (e: Exception) {
            false
        }.also { _isConnectingToHost.update { false } }
    }

    override suspend fun selectHostWithoutTest(url: String) {
        appSettingsDao.updateHostUrl(url)
    }
}