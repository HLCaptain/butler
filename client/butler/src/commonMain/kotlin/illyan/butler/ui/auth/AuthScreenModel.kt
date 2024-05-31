package illyan.butler.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.AuthManager
import illyan.butler.manager.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory

@Factory
class AuthScreenModel(
    authManager: AuthManager,
    hostManager: HostManager
) : ScreenModel {
    val state = combine(
        authManager.isUserSignedIn,
        hostManager.currentHost
    ) { isUserSignedIn, currentHost ->
        AuthState(
            isUserSignedIn = isUserSignedIn,
            hostSelected = currentHost != null
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState()
    )
}