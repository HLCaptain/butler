package illyan.butler.manager

import illyan.butler.repository.audio.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AudioManager(
    private val audioRepository: AudioRepository
) {
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    fun startRecording() {
        _isRecording.update { true }
    }

    fun stopRecording() {
        _isRecording.update { false }
    }
}