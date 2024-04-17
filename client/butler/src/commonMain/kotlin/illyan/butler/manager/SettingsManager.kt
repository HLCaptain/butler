package illyan.butler.manager

import illyan.butler.repository.AppSettingsRepository
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SettingsManager(
    appSettingsRepository: AppSettingsRepository
) {
    val userPreferences = appSettingsRepository.appSettings.map { it?.preferences }
}