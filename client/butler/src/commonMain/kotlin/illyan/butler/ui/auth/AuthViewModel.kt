package illyan.butler.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.AuthManager
import illyan.butler.manager.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Factory

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