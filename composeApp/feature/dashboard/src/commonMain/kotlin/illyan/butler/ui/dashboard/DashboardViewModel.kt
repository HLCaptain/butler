package illyan.butler.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.User
import illyan.butler.settings.SettingsManager
import illyan.butler.shared.model.chat.Source
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class DashboardViewModel(
    private val authManager: AuthManager,
    private val settingsManager: SettingsManager,
): ViewModel() {
    private val selectedUserSource = MutableStateFlow<Source.Server?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = combine(
        authManager.signedInUsers,
        settingsManager.appSettings,
        selectedUserSource.flatMapLatest { it?.let { authManager.getUser(it) } ?: flowOf(null) },
    ) { users, settings, selectedUser ->
        DashboardState(
            users = users.toPersistentSet(),
            selectedUser = selectedUser,
            appSettings = settings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DashboardState()
    )

    fun selectUser(user: User?) {
        selectedUserSource.update { user?.let { Source.Server(it.id, it.endpoint) } }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            authManager.upsertUserData(user)
        }
    }

    fun changeAppSettings(appSettings: AppSettings) {
        viewModelScope.launch {
            settingsManager.updateAppSettings(appSettings)
        }
    }
}
