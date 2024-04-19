package illyan.butler.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AppMemoryRepository : AppRepository {
    private val _appSettings = MutableStateFlow(null)
    override val appSettings = _appSettings.asStateFlow()

    private val _firstSignInHappenedYet = MutableStateFlow(false)
    override val firstSignInHappenedYet = _firstSignInHappenedYet.asStateFlow()

    private val _isTutorialDone = MutableStateFlow(false)
    override val isTutorialDone = _isTutorialDone.asStateFlow()

    override suspend fun setTutorialDone(isTutorialDone: Boolean) {
        _isTutorialDone.update { isTutorialDone }
    }
}