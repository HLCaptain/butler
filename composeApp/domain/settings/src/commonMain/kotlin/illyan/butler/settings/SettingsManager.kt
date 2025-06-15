package illyan.butler.settings

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.shared.model.chat.AiSource
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SettingsManager(
    private val appRepository: AppRepository
) {
    val userPreferences = appRepository.appSettings.map { it?.preferences }
    val defaultModel = appRepository.defaultModel
    suspend fun setUserPreferences(preferences: DomainPreferences) {
        appRepository.setUserPreferences(preferences)
    }
    suspend fun setDefaultModel(defaultModel: AiSource?) {
        appRepository.setDefaultModel(defaultModel)
    }
}
