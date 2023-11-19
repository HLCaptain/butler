package illyan.butler.ui.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.AuthManager
import org.koin.core.annotation.Factory

@Factory
class ProfileScreenModel(
    private val authManager: AuthManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
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

    val userPhotoUrl = authManager.signedInUser
        .map { it?.photoURL }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userUUID = authManager.signedInUser.map { it?.uid }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userEmail = authManager.signedInUser
        .map { it?.email }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userPhoneNumber = authManager.signedInUser
        .map { it?.phoneNumber }
        .stateIn(
            screenModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userName = authManager.signedInUser
        .map { it?.displayName }
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
}
