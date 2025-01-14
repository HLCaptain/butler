package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
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

    override val appSettings: Flow<AppSettings?> = _appSettings.asStateFlow()
    override val currentHost: Flow<String?> = _currentHost.asStateFlow()
    override val currentSignedInUserId: Flow<String?> = _currentSignedInUser.asStateFlow()

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        _appSettings.update { it.copy(preferences = preferences) }
    }

    override suspend fun setSignedInUser(userId: String?) {
        _currentSignedInUser.update { userId }
    }
}
