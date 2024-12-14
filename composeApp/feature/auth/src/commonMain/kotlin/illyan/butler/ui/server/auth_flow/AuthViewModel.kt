package illyan.butler.ui.server.auth_flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.host.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
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