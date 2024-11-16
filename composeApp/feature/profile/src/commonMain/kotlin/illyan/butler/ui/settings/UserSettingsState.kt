package illyan.butler.ui.settings

import illyan.butler.domain.model.DomainPreferences

data class UserSettingsState(
    val userPreferences: DomainPreferences? = null
)
