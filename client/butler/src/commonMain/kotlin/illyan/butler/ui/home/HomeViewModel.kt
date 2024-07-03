package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.Permission
import illyan.butler.manager.AppManager
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ErrorManager
import illyan.butler.manager.PermissionManager
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    authManager: AuthManager,
    private val appManager: AppManager,
    errorManager: ErrorManager,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    private val _serverErrors = MutableStateFlow<List<Pair<String, DomainErrorResponse>>>(listOf())
    private val _appErrors = MutableStateFlow<List<DomainErrorEvent>>(listOf())

    val state = combine(
        authManager.isUserSignedIn,
        authManager.signedInUserId,
        appManager.isTutorialDone,
        _serverErrors,
        _appErrors,
        permissionManager.preparedPermissionsToRequest
    ) { flows ->
        val isUserSignedIn = flows[0] as? Boolean
        val signedInUserUUID = flows[1] as? String
        val isTutorialDone = flows[2] as? Boolean
        val serverErrors = flows[3] as List<Pair<String, DomainErrorResponse>>
        val appErrors = flows[4] as List<DomainErrorEvent>
        val preparedPermissionsToRequest = flows[5] as Set<Permission>
        HomeScreenState(
            isUserSignedIn = isUserSignedIn,
            signedInUserUUID = signedInUserUUID,
            isTutorialDone = isTutorialDone,
            serverErrors = serverErrors,
            appErrors = appErrors,
            preparedPermissionsToRequest = preparedPermissionsToRequest,
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

    fun removeLastPermissionRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            permissionManager.removePermissionToRequest(state.value.preparedPermissionsToRequest.first())
        }
    }

    fun setTutorialDone() {
        viewModelScope.launch(Dispatchers.IO) {
            appManager.setTutorialDone()
        }
    }
}
