package illyan.butler.data.permission

actual fun getPlatformPermissionRepository(): PermissionRepository {
    return JvmPermissionRepository()
}