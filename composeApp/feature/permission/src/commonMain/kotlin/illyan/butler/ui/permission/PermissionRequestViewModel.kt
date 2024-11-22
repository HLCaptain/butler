package illyan.butler.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.Permission
import illyan.butler.permission.PermissionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PermissionRequestViewModel(
    private val permissionManager: PermissionManager
) : ViewModel() {
    val state = permissionManager.permissionStates.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyMap()
    )
    fun launchPermissionRequest(permission: Permission) {
        viewModelScope.launch {
            permissionManager.launchPermissionRequest(permission)
        }
    }
}
