package illyan.butler.ui.settings

import illyan.butler.domain.model.DomainPreferences

data class UserSettingsScreenState(
    val userPreferences: DomainPreferences? = null
)
