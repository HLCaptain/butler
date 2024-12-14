package illyan.butler.config

import illyan.butler.data.settings.AppRepository
import org.koin.core.annotation.Single

@Single
class AppManager(
    appRepository: AppRepository
) {
    val firstSignInHappenedYet = appRepository.firstSignInHappenedYet
}
