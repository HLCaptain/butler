package illyan.butler.ui.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory

@Factory
class HomeScreenModel(
    authManager: AuthManager,
) : ScreenModel {
    val signedInUserUUID = authManager.signedInUserUUID
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )
}
