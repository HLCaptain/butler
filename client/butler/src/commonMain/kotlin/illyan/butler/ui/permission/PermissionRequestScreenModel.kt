package illyan.butler.ui.permission

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.domain.model.Permission
import illyan.butler.manager.PermissionManager
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class PermissionRequestScreenModel(
    private val permissionManager: PermissionManager
) : ScreenModel {
    val state = permissionManager.preparedPermissionsToRequest
    fun launchPermissionRequest(permission: Permission) {
        screenModelScope.launch {
            permissionManager.launchPermissionRequest(permission)
        }
    }
}
