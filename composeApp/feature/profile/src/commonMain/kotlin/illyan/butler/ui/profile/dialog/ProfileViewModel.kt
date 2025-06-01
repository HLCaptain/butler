package illyan.butler.ui.profile.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val authManager: AuthManager,
): ViewModel() {
    val isUserSignedIn = authManager.isUserSignedIn
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val isUserSigningOut = flow { emit(false) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val userUUID = authManager.signedInUserId.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val signedInUser = authManager.signedInUser
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            authManager.signOut()
        }
    }

    fun resetTutorialAndSignOut() {
        viewModelScope.launch(Dispatchers.IO) {
            authManager.signOut()
        }
    }
}
