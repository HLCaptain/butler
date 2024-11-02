package illyan.butler.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.AuthManager
import illyan.butler.manager.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(
    authManager: AuthManager,
    hostManager: HostManager
) : ViewModel() {
    val state = combine(
        authManager.isUserSignedIn,
        hostManager.currentHost
    ) { isUserSignedIn, currentHost ->
        AuthState(
            isUserSignedIn = isUserSignedIn,
            hostSelected = currentHost != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState()
    )
}