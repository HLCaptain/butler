package illyan.butler.audio

actual fun getAudioRecorder(): AudioRecorder? {
    return null // WASM does not support audio recording yet
}
