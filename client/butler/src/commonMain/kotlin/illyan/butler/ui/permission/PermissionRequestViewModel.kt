package illyan.butler.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.model.Permission
import illyan.butler.manager.PermissionManager
import kotlinx.coroutines.launch

class PermissionRequestViewModel(
    private val permissionManager: PermissionManager
) : ViewModel() {
    val state = permissionManager.preparedPermissionsToRequest
    fun launchPermissionRequest(permission: Permission) {
        viewModelScope.launch {
            permissionManager.launchPermissionRequest(permission)
        }
    }
}
