package illyan.butler.repository.audio

import korlibs.audio.sound.AudioData

interface AudioRepository {
    suspend fun getMp3Audio(audioId: String): AudioData?
    suspend fun getOggAudio(audioId: String): AudioData?
    suspend fun getWavAudio(audioId: String): AudioData?
}