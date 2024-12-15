package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import kotlinx.coroutines.flow.StateFlow

interface AppRepository {
    val appSettings: StateFlow<AppSettings?>
    val firstSignInHappenedYet: StateFlow<Boolean?>
    val currentHost: StateFlow<String?>

    suspend fun setUserPreferences(preferences: DomainPreferences)
}