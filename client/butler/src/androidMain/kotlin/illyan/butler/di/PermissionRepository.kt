package illyan.butler.di

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.repository.permission.PermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AndroidPermissionRepository : PermissionRepository {
    companion object {
        private val ANDROID_PERMISSIONS = mapOf(
            Permission.CAMERA to android.Manifest.permission.CAMERA,
            Permission.GALLERY to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Permission.FINE_LOCATION to android.Manifest.permission.ACCESS_FINE_LOCATION,
            Permission.WRITE_EXTERNAL_STORAGE to android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    var activity: ComponentActivity? = null
        set(value) {
            field = value
            launcher = value?.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                refreshPermissionStatus(value, requestingPermission!!)
                requestingPermission = null
            }
        }
    private var requestingPermission: Permission? = null
    private var launcher: ActivityResultLauncher<String>? = null
    override val cachedPermissionFlows = MutableStateFlow(mapOf<Permission, PermissionStatus>())

    override fun getPermissionStatus(permission: Permission): Flow<PermissionStatus?> {
        activity?.let { refreshPermissionStatus(it, permission) }
        return cachedPermissionFlows.map { it[permission] }
    }

    private fun refreshPermissionStatus(context: Context, permission: Permission) {
        val hasPermission = context.checkPermission(ANDROID_PERMISSIONS[permission]!!)
        val status = if (hasPermission) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied(context.findActivity().shouldShowRationale(ANDROID_PERMISSIONS[permission]!!))
        }
        cachedPermissionFlows.update { it + (permission to status) }
    }

    override fun launchPermissionRequest(permission: Permission) {
        launcher?.let {
            requestingPermission = permission
            it.launch(ANDROID_PERMISSIONS[permission]!!)
        }
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.shouldShowRationale(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}
