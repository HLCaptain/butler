package illyan.butler.repository.app

import illyan.butler.data.local.room.dao.AppSettingsDao
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.di.KoinNames
import illyan.butler.model.AppSettings
import illyan.butler.model.DomainPreferences
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AppSettingsRoomRepository(
    private val appSettingsDao: AppSettingsDao,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : AppRepository {
    private val roomAppSettings = appSettingsDao.getAppSettings().stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )
    override val currentHost = roomAppSettings.map { it?.hostUrl }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )

    init {
        coroutineScopeIO.launch {
            if (!appSettingsDao.hasAppSettings().filterNotNull().first()) {
                Napier.d { "No app settings found, creating one" }
                appSettingsDao.upsertAppSettings(AppSettings.Default.toRoomModel())
            }
        }
    }

    override val appSettings = roomAppSettings.map { it?.toDomainModel() }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )
    override val firstSignInHappenedYet = roomAppSettings.map { it?.firstSignInHappenedYet }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )
    override val isTutorialDone = roomAppSettings.map { it?.isTutorialDone }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )
    override suspend fun setTutorialDone(isTutorialDone: Boolean) {
        appSettingsDao.updateTutorialDone(isTutorialDone)
    }
    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        appSettingsDao.updatePreferences(preferences)
    }
}