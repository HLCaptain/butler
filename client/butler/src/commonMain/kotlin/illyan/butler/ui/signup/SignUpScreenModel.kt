package illyan.butler.ui.signup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class SignUpScreenModel(
    private val authManager: AuthManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    val state = combine(
        authManager.isUserSignedIn,
        authManager.isUserSigningIn
    ) { isUserSignedIn, isUserSigningIn ->
        SignUpScreenState(isUserSignedIn, isUserSigningIn)
    }.stateIn(screenModelScope, SharingStarted.Eagerly, SignUpScreenState())

    fun signUpAndLogin(email: String,  password: String, userName: String) {
        screenModelScope.launch(dispatcherIO) {
            authManager.signUpAndLogin(email, password, userName)
        }
    }
}