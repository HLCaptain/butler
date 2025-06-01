package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AppRepository {
    val appSettings: Flow<AppSettings?>
    val currentHost: Flow<String?>
    val currentSignedInUserId: Flow<String?>
    val defaultModel: Flow<ModelConfig?>
    val isUserSignedIn: Flow<Boolean>
        get() = currentSignedInUserId.map { it != null }

    suspend fun setUserPreferences(preferences: DomainPreferences)
    suspend fun setSignedInUser(userId: String?)
    suspend fun setDefaultModel(model: ModelConfig?)
}
