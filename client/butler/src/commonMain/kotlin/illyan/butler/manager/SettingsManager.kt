package illyan.butler.manager

import illyan.butler.domain.model.DomainPreferences
import illyan.butler.repository.AppRepository
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