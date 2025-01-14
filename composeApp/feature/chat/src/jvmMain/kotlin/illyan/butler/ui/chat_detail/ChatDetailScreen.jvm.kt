package illyan.butler.ui.chat_detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ChatDetailBottomBar(
    modifier: Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (ByteArray, String) -> Unit,
    isRecording: Boolean,
    toggleRecord: () -> Unit
) {
    MessageField(
        modifier = modifier,
        sendMessage = sendMessage,
        isRecording = isRecording,
        toggleRecord = toggleRecord,
        sendImage = sendImage,
        galleryAccessGranted = true, // Desktop doesn't need permission
        galleryEnabled = true, // Desktop doesn't need permission
        recordAudioAccessGranted = true, // Desktop doesn't need permission
        recordAudioEnabled = true, // Desktop doesn't need permission
        requestGalleryAccess = {},
        requestRecordAudioAccess = {},
    )
}