package illyan.butler.ui.settings.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.model.Theme
import illyan.butler.manager.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserSettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val state = settingsManager.userPreferences.map { userPreferences ->
        UserSettingsScreenState(userPreferences = userPreferences)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettingsScreenState()
    )

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            state.value.userPreferences?.copy(theme = theme)?.let { userPreferences ->
                settingsManager.setUserPreferences(userPreferences)
            }
        }
    }

    fun setDynamicColorEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            state.value.userPreferences?.copy(dynamicColorEnabled = isEnabled)?.let { userPreferences ->
                settingsManager.setUserPreferences(userPreferences)
            }
        }
    }
}