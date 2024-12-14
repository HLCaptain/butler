package illyan.butler.data.settings

import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.core.local.room.model.RoomAppSettings
import illyan.butler.core.local.room.dao.AppSettingsDao
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

@Single
class AppSettingsRoomRepository(
    private val appSettingsDao: AppSettingsDao,
) : AppRepository {
    private val roomAppSettings: StateFlow<RoomAppSettings?> by lazy {
        appSettingsDao.getAppSettings().map {
            if (it == null) {
                Napier.d { "No app settings found, creating one" }
                appSettingsDao.upsertAppSettings(AppSettings.Default.toRoomModel())
            }
            it
        }.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            null
        )
    }
    override val currentHost: StateFlow<String?> by lazy {
        roomAppSettings.map { it?.hostUrl }.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            null
        )
    }

    override val appSettings: StateFlow<AppSettings?> by lazy {
        roomAppSettings.map { it?.toDomainModel() }.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            null
        )
    }
    override val firstSignInHappenedYet = roomAppSettings.map { it?.firstSignInHappenedYet }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.Eagerly,
        null
    )
    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        appSettingsDao.updatePreferences(preferences.toRoomModel())
    }
}