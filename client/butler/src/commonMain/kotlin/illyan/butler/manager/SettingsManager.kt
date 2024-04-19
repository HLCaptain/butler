package illyan.butler.manager

import illyan.butler.repository.AppRepository
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SettingsManager(
    appRepository: AppRepository
) {
    val userPreferences = appRepository.appSettings.map { it?.preferences }
}