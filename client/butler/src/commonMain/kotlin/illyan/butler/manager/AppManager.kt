package illyan.butler.manager

import illyan.butler.repository.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class AppManager(
    val appSettingsRepository: AppSettingsRepository
) {
    val firstSignInHappenedYet = appSettingsRepository.firstSignInHappenedYet
}