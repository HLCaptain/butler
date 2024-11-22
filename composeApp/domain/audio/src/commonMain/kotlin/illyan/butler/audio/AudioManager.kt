package illyan.butler.audio

import illyan.butler.domain.model.DomainResource
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.permission.PermissionManager
import illyan.butler.data.resource.ResourceRepository
import io.github.aakira.napier.Napier
import korlibs.audio.format.MP3
import korlibs.audio.format.WAV
import korlibs.audio.format.toWav
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.nativeSoundProvider
import korlibs.audio.sound.toStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class AudioManager(
    private val audioRecorder: AudioRecorder?,
//    private val audioPlayer: AudioPlayer?,
//    private val nativeSoundChannel: NativeSoundProviderNew,
    private val resourceRepository: ResourceRepository,
    private val permissionManager: PermissionManager,
) {
    private val _playingAudioId = MutableStateFlow<String?>(null)
    private val _playingSoundOnChannel = MutableStateFlow<SoundChannel?>(null)
    val playingAudioId = _playingAudioId.asStateFlow()
    val isPlaying = _playingAudioId.map { it != null }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, false)
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
                type = Wav.toString(),
                data = audioData.toWav()
            )
        )
    }

    suspend fun playAudio(audioId: String) {
        val resource = resourceRepository.getResourceFlow(audioId).first { !it.second }.first!!
        val audioData = when (resource.type) {
            "audio/wav" -> WAV.decode(resource.data)
            "audio/mp3" -> MP3.decode(resource.data)
            else -> throw IllegalArgumentException("Unsupported audio type: ${resource.type}")
        }
        _playingAudioId.update { audioId }
//        nativeSoundChannel.playAndWait(audioData!!.toStream())
        Napier.d("Playing audio: $audioData")
        val channel = nativeSoundProvider.createStreamingSound(audioData!!.toStream()) {
            Napier.d("Audio completed: $audioId")
            stopAudio()
        }.play()
        _playingSoundOnChannel.update { channel }
    }

    suspend fun stopAudio() {
        _playingAudioId.update { null }
        _playingSoundOnChannel.update { it?.stop(); null }
//        nativeSoundChannel.stop()
//        nativeSoundProvider
    }
}