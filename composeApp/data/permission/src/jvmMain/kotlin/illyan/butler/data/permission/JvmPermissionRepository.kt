package illyan.butler.data.permission

import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

/**
 * FIXME: JVM implementation is currently dummy.
 */
@Single
class JvmPermissionRepository : PermissionRepository {
    override val cachedPermissionFlows = MutableStateFlow(mapOf<Permission, PermissionStatus>())

    override fun getPermissionStatus(permission: Permission): Flow<PermissionStatus?> {
        cachedPermissionFlows.update { it + (permission to PermissionStatus.Granted) }
        return cachedPermissionFlows.map { it[permission] }
    }

    override fun launchPermissionRequest(permission: Permission) {
        cachedPermissionFlows.update { it + (permission to PermissionStatus.Granted) }
    }

    override fun showAppRationale(permission: Permission) {
        cachedPermissionFlows.update { it + (permission to PermissionStatus.ShowAppRationale) }
    }
}