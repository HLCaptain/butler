package illyan.butler.ui.profile.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
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

    val userUUID = authManager.signedInServers
        .map { it.firstOrNull()?.userId.toString() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val signedInUser = authManager.signedInServers
        .flatMapLatest { servers ->
            servers.firstOrNull()?.let { authManager.getUser(it) } ?: flowOf(null)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun signOut() {
        viewModelScope.launch {
            signedInUser.firstOrNull()?.let { authManager.signOut(Source.Server(it.id, it.endpoint)) }
        }
    }

    fun resetTutorialAndSignOut() {
        viewModelScope.launch {
            signedInUser.firstOrNull()?.let { authManager.signOut(Source.Server(it.id, it.endpoint)) }
        }
    }
}
