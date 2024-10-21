package illyan.butler.repository.permission

import illyan.butler.model.Permission
import illyan.butler.model.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PermissionRepository {
    val cachedPermissionFlows: StateFlow<Map<Permission, PermissionStatus>>
    fun getPermissionStatus(permission: Permission): Flow<PermissionStatus?>
    fun launchPermissionRequest(permission: Permission)
}