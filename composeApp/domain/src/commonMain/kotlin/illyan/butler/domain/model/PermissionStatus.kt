package illyan.butler.domain.model

sealed class PermissionStatus {
    data object Unknown : PermissionStatus()
    data object NotSupported : PermissionStatus()
    data object Granted : PermissionStatus()
    data class Denied(val shouldShowRationale: Boolean) : PermissionStatus()
}
