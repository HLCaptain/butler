package illyan.butler.manager

import illyan.butler.domain.model.DomainResource
import illyan.butler.repository.resource.ResourceRepository
import illyan.butler.utils.Wav
import illyan.butler.utils.audio.AudioRecorder
import io.ktor.http.ContentType
import korlibs.audio.format.WAV
import korlibs.audio.format.toWav
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AudioManager(
    private val audioRecorder: AudioRecorder?,
//    private val audioPlayer: AudioPlayer?,
//    private val nativeSoundChannel: NativeSoundProviderNew,
    private val resourceRepository: ResourceRepository
) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
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

    suspend fun playAudio(audioId: String) {
        val resource = resourceRepository.getResourceFlow(audioId).first { !it.second }.first!!
        val audioData = when (resource.type) {
            ContentType.Audio.Wav.toString() -> WAV.decode(resource.data)
            else -> throw IllegalArgumentException("Unsupported audio type: ${resource.type}")
        }
        _isPlaying.update { true }
//        nativeSoundChannel.playAndWait(audioData!!.toStream())
    }

    suspend fun stopAudio() {
        _isPlaying.update { false }
//        nativeSoundChannel.stop()
    }
}