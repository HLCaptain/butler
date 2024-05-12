package illyan.butler.domain.model

sealed class PermissionStatus {
    open val shouldShowRationale: Boolean = false

    data object Granted : PermissionStatus()
    data class Denied(override val shouldShowRationale: Boolean) : PermissionStatus()
}
