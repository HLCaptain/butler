package illyan.butler.repository

import korlibs.audio.sound.AudioStream

interface AudioRepository {
    fun getMp3Audio(audioId: String): AudioStream?
    fun getOggAudio(audioId: String): AudioStream?
    fun getWavAudio(audioId: String): AudioStream?
}