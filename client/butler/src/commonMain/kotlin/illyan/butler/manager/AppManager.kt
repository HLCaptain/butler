package illyan.butler.manager

import illyan.butler.repository.app.AppRepository
import org.koin.core.annotation.Single

@Single
class AppManager(
    val appRepository: AppRepository
) {
    val firstSignInHappenedYet = appRepository.firstSignInHappenedYet
    val isTutorialDone = appRepository.isTutorialDone

    suspend fun setTutorialDone() {
        appRepository.setTutorialDone(true)
    }

    suspend fun resetTutorial() {
        appRepository.setTutorialDone(false)
    }
}