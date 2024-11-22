package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.config.AppManager
import illyan.butler.core.utils.randomUUID
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.error.ErrorManager
import illyan.butler.permission.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    authManager: AuthManager,
    private val appManager: AppManager,
    errorManager: ErrorManager,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    private val _serverErrors = MutableStateFlow<List<Pair<String, DomainErrorResponse>>>(listOf())
    private val _appErrors = MutableStateFlow<List<DomainErrorEvent>>(listOf())

    private val _permissionStatuses = permissionManager.permissionStates.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyMap()
    )

    val state = combine(
        authManager.isUserSignedIn,
        appManager.isTutorialDone,
        _serverErrors,
        _appErrors,
        _permissionStatuses
    ) { flows ->
        val isUserSignedIn = flows[0] as? Boolean
        val isTutorialDone = flows[1] as? Boolean
        val serverErrors = flows[2] as List<Pair<String, DomainErrorResponse>>
        val appErrors = flows[3] as List<DomainErrorEvent>
        val permissionStatuses = flows[4] as Map<Permission, PermissionStatus?>
        if (isTutorialDone == null || isUserSignedIn == null) return@combine HomeScreenState()
        HomeScreenState(
            isUserSignedIn = isUserSignedIn,
            isTutorialDone = isTutorialDone,
            serverErrors = serverErrors,
            appErrors = appErrors,
            permissionStatuses = permissionStatuses
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeScreenState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            errorManager.serverErrors.collectLatest { response ->
                _serverErrors.update { it + (randomUUID() to  response) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            errorManager.appErrors.collectLatest { error ->
                _appErrors.update { it + error }
            }
        }
    }

    fun clearError(id: String) {
        _serverErrors.update { errors ->
            errors.filter { it.first != id }
        }
        _appErrors.update { errors ->
            errors.filter { it.id != id }
        }
    }

    fun removeLastError() {
        viewModelScope.launch(Dispatchers.IO) {
            val latestServerErrorId = _serverErrors.first().maxByOrNull { it.second.timestamp }?.first
            val latestAppErrorId = _appErrors.first().maxByOrNull { it.timestamp }?.id
            if (latestServerErrorId != null && latestAppErrorId != null) {
                if (latestServerErrorId > latestAppErrorId) {
                    clearError(latestServerErrorId)
                } else {
                    clearError(latestAppErrorId)
                }
            } else if (latestServerErrorId != null) {
                clearError(latestServerErrorId)
            } else if (latestAppErrorId != null) {
                clearError(latestAppErrorId)
            }
        }
    }

    fun setTutorialDone() {
        viewModelScope.launch(Dispatchers.IO) {
            appManager.setTutorialDone()
        }
    }

    fun dismissPermissionRequest(permission: Permission?) {
        permission?.let {
            viewModelScope.launch(Dispatchers.IO) {
                permissionManager.closeAppRationale(it)
            }
        }
    }

    fun launchPermissionRequest(permission: Permission?) {
        permission?.let {
            viewModelScope.launch(Dispatchers.IO) {
                permissionManager.launchPermissionRequest(it)
            }
        }
    }
}
