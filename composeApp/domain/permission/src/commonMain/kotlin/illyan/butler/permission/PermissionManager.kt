package illyan.butler.permission

import illyan.butler.data.permission.PermissionRepository
import illyan.butler.domain.model.Permission
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class PermissionManager(private val permissionRepository: PermissionRepository) {
    val permissionStates = combine(
        Permission.entries.map { permission -> permissionRepository.getPermissionStatus(permission).map { permission to it } }
    ) { it.toMap() }

    fun getPermissionStatus(permission: Permission) = permissionRepository.getPermissionStatus(permission).also {
        Napier.d("getPermissionStatus($permission)")
    }

    fun launchPermissionRequest(permission: Permission) = permissionRepository.launchPermissionRequest(permission).also {
        Napier.d("launchPermissionRequest($permission)")
    }

    fun showAppRationale(permission: Permission) = permissionRepository.showAppRationale(permission).also {
        Napier.d("showAppRationale($permission)")
    }

    fun closeAppRationale(permission: Permission) = permissionRepository.removePermissionStatus(permission).also {
        Napier.d("closeAppRationale($permission)")
    }
}