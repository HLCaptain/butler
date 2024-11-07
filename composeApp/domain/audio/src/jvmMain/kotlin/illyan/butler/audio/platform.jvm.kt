package illyan.butler.audio

actual fun getAudioRecorder(): AudioRecorder? {
    return JvmAudioRecorder()
}