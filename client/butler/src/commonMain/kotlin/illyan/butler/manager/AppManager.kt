package illyan.butler.manager

import illyan.butler.di.KoinNames
import illyan.butler.repository.app.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AppManager(
    val appRepository: AppRepository,
    val authManager: AuthManager,
    @Named(KoinNames.CoroutineScopeIO) val coroutineScopeIO: CoroutineScope
) {
    val firstSignInHappenedYet = appRepository.firstSignInHappenedYet
    val isTutorialDone = appRepository.isTutorialDone

    init {
        coroutineScopeIO.launch {
            // If the user is signed in on the start of the app, set the tutorial as done
            authManager.isUserSignedIn.value?.let { if (it) setTutorialDone() }
        }
    }
    suspend fun setTutorialDone() {
        appRepository.setTutorialDone(true)
    }

    suspend fun resetTutorial() {
        appRepository.setTutorialDone(false)
    }
}