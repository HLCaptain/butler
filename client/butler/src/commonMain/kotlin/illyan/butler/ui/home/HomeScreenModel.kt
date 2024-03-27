package illyan.butler.ui.home

import cafe.adriel.voyager.core.model.ScreenModel
import illyan.butler.manager.AppManager
import illyan.butler.manager.AuthManager
import org.koin.core.annotation.Factory

@Factory
class HomeScreenModel(
    authManager: AuthManager,
    appManager: AppManager
) : ScreenModel {
    val isUserSignedIn = authManager.isUserSignedIn
    val signedInUserUUID = authManager.signedInUserUUID
    val isTutorialDone = appManager.isTutorialDone
}
