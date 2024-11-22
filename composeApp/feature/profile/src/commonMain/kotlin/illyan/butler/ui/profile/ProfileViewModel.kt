package illyan.butler.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.config.AppManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val authManager: AuthManager,
    private val appManager: AppManager,
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

    val userPhotoUrl = authManager.signedInUserPhotoURL
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userUUID = authManager.signedInUserId
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userEmail = authManager.signedInUserEmail
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userPhoneNumber = authManager.signedInUserPhoneNumber
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val userName = authManager.signedInUserName
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
            appManager.resetTutorial()
            authManager.signOut()
        }
    }
}
