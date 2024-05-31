package illyan.butler.repository.permission

import illyan.butler.di.KoinNames
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class MemoryPermissionRepository(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : PermissionRepository {
    override val cachedPermissionFlows = MutableStateFlow(mapOf<Permission, PermissionStatus>())

    override fun getPermissionStatus(permission: Permission): Flow<PermissionStatus?> {
        if (!cachedPermissionFlows.value.containsKey(permission)) {
            coroutineScopeIO.launch {
                cachedPermissionFlows.update { it + (permission to PermissionStatus.Denied(true)) }
            }
        }
        return cachedPermissionFlows.map { it[permission] }
    }

    override fun launchPermissionRequest(permission: Permission) {
        coroutineScopeIO.launch {
            cachedPermissionFlows.update { it + (permission to PermissionStatus.Granted) }
        }
    }
}