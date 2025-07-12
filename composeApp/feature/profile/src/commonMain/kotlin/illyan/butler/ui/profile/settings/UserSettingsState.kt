package illyan.butler.ui.profile.settings

import illyan.butler.domain.model.Preferences

data class UserSettingsState(
    val userPreferences: Preferences? = null
)
