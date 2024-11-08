package illyan.butler.config

import illyan.butler.auth.AuthManager
import illyan.butler.data.settings.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class AppManager(
    private val appRepository: AppRepository,
    private val authManager: AuthManager,
) {
    val firstSignInHappenedYet = appRepository.firstSignInHappenedYet
    val isTutorialDone = appRepository.isTutorialDone.map {
        // If the user is signed in on the start of the app, set the tutorial as done
        if (authManager.isUserSignedIn.filterNotNull().first()) setTutorialDone()
        it
    }.stateIn(
        CoroutineScope(Dispatchers.IO),
        SharingStarted.Eagerly,
        null
    )

    suspend fun setTutorialDone() {
        appRepository.setTutorialDone(true)
    }

    suspend fun resetTutorial() {
        appRepository.setTutorialDone(false)
    }
}