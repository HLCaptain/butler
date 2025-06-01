package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.ModelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AppMemoryRepository : AppRepository {
    private val _appSettings = MutableStateFlow(AppSettings.Default)
    private val _currentHost = MutableStateFlow<String?>(null)
    private val _currentSignedInUser = MutableStateFlow<String?>(null)
    private val _defaultModel = MutableStateFlow<ModelConfig?>(null)

    override val appSettings: Flow<AppSettings?> = _appSettings.asStateFlow()
    override val currentHost: Flow<String?> = _currentHost.asStateFlow()
    override val currentSignedInUserId: Flow<String?> = _currentSignedInUser.asStateFlow()
    override val defaultModel: Flow<ModelConfig?> = _defaultModel.asStateFlow()

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        _appSettings.update { it.copy(preferences = preferences) }
    }

    override suspend fun setSignedInUser(userId: String?) {
        _currentSignedInUser.update { userId }
    }

    override suspend fun setDefaultModel(model: ModelConfig?) {
        _defaultModel.update { model }
    }
}
