package illyan.butler.ui.chat_detail

@androidx.compose.runtime.Composable
actual fun ChatDetailBottomBar(
    modifier: androidx.compose.ui.Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (ByteArray, String) -> Unit,
    isRecording: Boolean,
    toggleRecord: () -> Unit,
    enabled: Boolean,
    currentModel: illyan.butler.shared.model.chat.AiSource?
) {
    // FIXME: make iOS support permissions
    MessageField(
        modifier = modifier,
        sendMessage = sendMessage,
        isRecording = isRecording,
        toggleRecord = toggleRecord,
        sendImage = sendImage,
        galleryAccessGranted = false,
        galleryEnabled = false,
        recordAudioAccessGranted = false,
        recordAudioEnabled = false,
        requestGalleryAccess = {},
        requestRecordAudioAccess = {},
        enabled = enabled,
        currentModel = currentModel
    )
}
