package illyan.butler.ui.permission

import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.permission_request_gallery_description
import illyan.butler.generated.resources.permission_request_gallery_title
import illyan.butler.generated.resources.permission_request_record_audio_description
import illyan.butler.generated.resources.permission_request_record_audio_title
import org.jetbrains.compose.resources.StringResource

private const val MICROPHONE_PERMISSION = "microphone_permission"
private const val PHOTO_LIBRARY_PERMISSION = "photo_library_permission"

actual val platformSpecificPermissions: Map<String, Pair<StringResource?, StringResource?>>
    get() = mapOf(
        MICROPHONE_PERMISSION to (Res.string.permission_request_record_audio_title to Res.string.permission_request_record_audio_description),
        PHOTO_LIBRARY_PERMISSION to (Res.string.permission_request_gallery_title to Res.string.permission_request_gallery_description),
    )
