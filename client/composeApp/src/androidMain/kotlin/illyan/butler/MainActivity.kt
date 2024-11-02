package illyan.butler

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import illyan.butler.di.AndroidPermissionRepository

class MainActivity : AppCompatActivity() {
    private lateinit var permissionRepository: AndroidPermissionRepository
    lateinit var permissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRepository = getPlatformPermissionRepository() as AndroidPermissionRepository
        permissionRepository.activity = this
//        permissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) {
//            permissionRepository.refreshPermissionStatus(requestingPermission!!)
//            permissionRepository.requestingPermission = null
//        }
        setContent {
            App()
        }
    }
}
