package illyan.butler.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authManager: AuthManager,
) : ViewModel() {
    val state = combine(
        authManager.isUserSignedIn,
        authManager.isUserSigningIn
    ) { isUserSignedIn, isUserSigningIn ->
        SignUpScreenState(isUserSignedIn, isUserSigningIn)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SignUpScreenState())

    fun signUpAndLogin(email: String, password: String, userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authManager.signUpAndLogin(email, password, userName)
        }
    }
}