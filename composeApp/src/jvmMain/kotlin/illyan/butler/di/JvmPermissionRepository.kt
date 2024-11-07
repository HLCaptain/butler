package illyan.butler.di

import illyan.butler.model.Permission
import illyan.butler.model.PermissionStatus
import illyan.butler.data.permission.JvmPermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class JvmPermissionRepository :
    JvmPermissionRepository {
    override val cachedPermissionFlows = MutableStateFlow(mapOf<Permission, PermissionStatus>())

    override fun getPermissionStatus(permission: Permission): Flow<PermissionStatus?> {
        return cachedPermissionFlows.map { it[permission] }.also {
            cachedPermissionFlows.update { it + (permission to PermissionStatus.Granted) }
        }
    }

    override fun launchPermissionRequest(permission: Permission) {
        cachedPermissionFlows.update { it + (permission to PermissionStatus.Granted) }
    }
}