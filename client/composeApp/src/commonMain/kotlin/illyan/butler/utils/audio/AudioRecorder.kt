package illyan.butler.utils.audio

import korlibs.audio.sound.AudioData
import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val isRecording: StateFlow<Boolean>
    suspend fun startRecording()
    suspend fun stopRecording(): AudioData
}