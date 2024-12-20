package illyan.butler.settings

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.DomainPreferences
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SettingsManager(
    private val appRepository: AppRepository
) {
    val userPreferences = appRepository.appSettings.map { it?.preferences }
    suspend fun setUserPreferences(preferences: DomainPreferences) {
        appRepository.setUserPreferences(preferences)
    }
}