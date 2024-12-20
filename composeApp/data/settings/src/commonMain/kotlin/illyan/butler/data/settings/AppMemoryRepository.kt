package illyan.butler.data.settings

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AppMemoryRepository : AppRepository {
    private val _appSettings = MutableStateFlow(AppSettings.Default)
    override val appSettings = _appSettings.asStateFlow()

    private val _firstSignInHappenedYet = MutableStateFlow(false)
    override val firstSignInHappenedYet: StateFlow<Boolean?> = _firstSignInHappenedYet.asStateFlow()

    private val _isTutorialDone = MutableStateFlow(false)
    override val isTutorialDone = _isTutorialDone.asStateFlow()
    override val currentHost = MutableStateFlow<String?>(null).asStateFlow()

    override suspend fun setTutorialDone(isTutorialDone: Boolean) {
        Napier.d { "Setting tutorial done to $isTutorialDone" }
        _isTutorialDone.update { isTutorialDone }
    }

    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        Napier.d { "Setting user preferences to $preferences" }
        _appSettings.update { it.copy(preferences = preferences) }
    }
}