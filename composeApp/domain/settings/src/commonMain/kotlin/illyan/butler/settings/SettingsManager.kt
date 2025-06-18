package illyan.butler.settings

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.PromptConfiguration
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SettingsManager(
    private val appRepository: AppRepository,
) {
    val userPreferences = appRepository.appSettings.map { it.preferences }
    val appSettings = appRepository.appSettings
    val deviceFilterConfigurations = appRepository.appSettings.map { it.filterConfiguration }
    val selectedPromptConfiguration = appRepository.selectedPromptConfiguration
    val defaultModel = appRepository.defaultModel
    suspend fun setUserPreferences(preferences: DomainPreferences) {
        appRepository.setUserPreferences(preferences)
    }
    suspend fun setDefaultModel(defaultModel: AiSource?) {
        appRepository.setDefaultModel(defaultModel)
    }

    suspend fun setFilterConfiguration(filterConfiguration: FilterConfiguration) {
        appRepository.setFilterConfiguration(filterConfiguration)
    }

    suspend fun updateAppSettings(appSettings: AppSettings) {
        appRepository.setAppSettings(appSettings)
    }

    suspend fun setSelectedPromptConfiguration(promptConfiguration: PromptConfiguration?) {
        appRepository.setSelectedPromptConfiguration(promptConfiguration)
    }
}
