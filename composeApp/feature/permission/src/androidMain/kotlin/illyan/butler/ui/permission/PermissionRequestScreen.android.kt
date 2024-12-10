package illyan.butler.ui.permission

import android.Manifest
import android.os.Build
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.permission_request_gallery_description
import illyan.butler.generated.resources.permission_request_gallery_title
import illyan.butler.generated.resources.permission_request_record_audio_description
import illyan.butler.generated.resources.permission_request_record_audio_title
import org.jetbrains.compose.resources.StringResource

private val galleryPermissionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
actual val platformSpecificPermissions: Map<String, Pair<StringResource?, StringResource?>>
    get() = mapOf(
        Manifest.permission.RECORD_AUDIO to (Res.string.permission_request_record_audio_title to Res.string.permission_request_record_audio_description),
        galleryPermissionCode to (Res.string.permission_request_gallery_title to Res.string.permission_request_gallery_description),
    )