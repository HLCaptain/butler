package illyan.butler.ui.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.manager.AppManager
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ProfileScreenModel(
    private val authManager: AuthManager,
    private val appManager: AppManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher
): ScreenModel {
    val isUserSignedIn = authManager.isUserSignedIn
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            false
        )

    val isUserSigningOut = flow { emit(false) }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            false
        )

    val userPhotoUrl = authManager.signedInUserPhotoURL
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userUUID = authManager.signedInUserUUID
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userEmail = authManager.signedInUserEmail
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userPhoneNumber = authManager.signedInUserPhoneNumber
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userName = authManager.signedInUserName
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun signOut() {
        screenModelScope.launch(dispatcherIO) {
            authManager.signOut()
        }
    }

    fun resetTutorialAndSignOut() {
        screenModelScope.launch(dispatcherIO) {
            appManager.resetTutorial()
            authManager.signOut()
        }
    }
}
