package illyan.butler.repository.app

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AppMemoryRepository : AppRepository {
    private val _appSettings = MutableStateFlow(AppSettings.default)
    override val appSettings = _appSettings.asStateFlow()

    private val _firstSignInHappenedYet = MutableStateFlow(false)
    override val firstSignInHappenedYet = _firstSignInHappenedYet.asStateFlow()

    private val _isTutorialDone = MutableStateFlow(false)
    override val isTutorialDone = _isTutorialDone.asStateFlow()

    override suspend fun setTutorialDone(isTutorialDone: Boolean) {
        _isTutorialDone.update { isTutorialDone }
    }

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        _appSettings.update { it.copy(preferences = preferences) }
    }
}