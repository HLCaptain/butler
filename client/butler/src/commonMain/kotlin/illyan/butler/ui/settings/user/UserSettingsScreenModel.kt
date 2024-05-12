package illyan.butler.ui.settings.user

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.domain.model.Theme
import illyan.butler.manager.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class UserSettingsScreenModel(
    private val settingsManager: SettingsManager
) : ScreenModel {

    val state = settingsManager.userPreferences.map { userPreferences ->
        UserSettingsScreenState(
            userPreferences = userPreferences
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettingsScreenState()
    )

    fun setTheme(theme: Theme) {
        screenModelScope.launch {
            state.value.userPreferences?.copy(theme = theme)?.let { userPreferences ->
                settingsManager.setUserPreferences(userPreferences)
            }
        }
    }

    fun setDynamicColorEnabled(isEnabled: Boolean) {
        screenModelScope.launch {
            state.value.userPreferences?.copy(dynamicColorEnabled = isEnabled)?.let { userPreferences ->
                settingsManager.setUserPreferences(userPreferences)
            }
        }
    }
}