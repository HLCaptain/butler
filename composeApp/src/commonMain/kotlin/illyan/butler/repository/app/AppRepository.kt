package illyan.butler.repository.app

import illyan.butler.model.AppSettings
import illyan.butler.model.DomainPreferences
import kotlinx.coroutines.flow.StateFlow

interface AppRepository {
    val appSettings: StateFlow<AppSettings?>
    val firstSignInHappenedYet: StateFlow<Boolean?>
    val isTutorialDone: StateFlow<Boolean?>
    val currentHost: StateFlow<String?>

    suspend fun setTutorialDone(isTutorialDone: Boolean)
    suspend fun setUserPreferences(preferences: DomainPreferences)
}