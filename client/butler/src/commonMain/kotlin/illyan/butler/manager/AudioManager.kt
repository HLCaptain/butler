package illyan.butler.manager

import illyan.butler.domain.model.DomainResource
import illyan.butler.repository.resource.ResourceRepository
import illyan.butler.utils.AudioRecorder
import korlibs.audio.format.toWav
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class AudioManager(
    private val audioRecorder: AudioRecorder?,
    private val resourceRepository: ResourceRepository
) {
    val isRecording = audioRecorder?.isRecording ?: MutableStateFlow(false).asStateFlow()
    val canRecordAudio = audioRecorder != null

    suspend fun startRecording() {
        if (audioRecorder == null) throw IllegalStateException("Audio recording is not supported")
        audioRecorder.startRecording()
    }

    suspend fun stopRecording(): String {
        if (audioRecorder == null) throw IllegalStateException("Audio recording is not supported")
        val audioData = audioRecorder.stopRecording()
        return resourceRepository.upsert(
            DomainResource(
                type = "audio/wav", // WAV,
                data = audioData.toWav()
            )
        )
    }
}