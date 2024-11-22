package illyan.butler.data.permission

import org.koin.core.context.GlobalContext

actual fun getPlatformPermissionRepository(): PermissionRepository {
    return GlobalContext.get().get<AndroidPermissionRepository>()
}