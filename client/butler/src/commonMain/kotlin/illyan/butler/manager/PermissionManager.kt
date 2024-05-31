package illyan.butler.manager

import illyan.butler.di.KoinNames
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.repository.permission.PermissionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class PermissionManager(
    private val permissionRepository: PermissionRepository,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) {
    private val _preparedPermissionsToRequest = MutableStateFlow(setOf<Permission>())
    val preparedPermissionsToRequest = _preparedPermissionsToRequest.asStateFlow()

    init {
        coroutineScopeIO.launch {
            permissionRepository.cachedPermissionFlows.collect { cached ->
                val grantedPermissions = cached.filterValues { it is PermissionStatus.Granted }.keys
                val newGrantedPermissions = grantedPermissions - _preparedPermissionsToRequest.value
                _preparedPermissionsToRequest.update {
                    it.filterNot { permission -> cached[permission] is PermissionStatus.Granted }.toSet()
                }
                Napier.d("Granted permissions: $newGrantedPermissions")
            }
        }
    }

    fun getPermissionStatus(permission: Permission) = permissionRepository.getPermissionStatus(permission).also {
        Napier.d("getPermissionStatus($permission)")
    }

    suspend fun preparePermissionRequest(permission: Permission) {
        _preparedPermissionsToRequest.update {
            val currentStatus = getPermissionStatus(permission).filterNotNull().first()
            Napier.d("preparePermissionRequest($permission) = $currentStatus")
            if (currentStatus is PermissionStatus.Denied) it + permission else it
        }
    }

    fun launchPermissionRequest(permission: Permission) = permissionRepository.launchPermissionRequest(permission).also {
        Napier.d("launchPermissionRequest($permission)")
    }

    fun removePermissionToRequest(permission: Permission) = _preparedPermissionsToRequest.update { it - permission }
}