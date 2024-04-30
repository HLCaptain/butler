package illyan.butler.repository.audio

import illyan.butler.repository.resource.ResourceRepository
import korlibs.audio.format.MP3
import korlibs.audio.format.OGG
import korlibs.audio.format.WAV
import korlibs.audio.sound.AudioData
import org.koin.core.annotation.Single

@Single
class AudioMemoryRepository(
    private val resourceRepository: ResourceRepository
) : AudioRepository {
    override suspend fun getMp3Audio(audioId: String): AudioData? {
        return MP3.decode(resourceRepository.getResourceFlow(audioId)?.data ?: ByteArray(0))
    }

    override suspend fun getOggAudio(audioId: String): AudioData? {
        return OGG.decode(resourceRepository.getResourceFlow(audioId)?.data ?: ByteArray(0))
    }

    override suspend fun getWavAudio(audioId: String): AudioData? {
        return WAV.decode(resourceRepository.getResourceFlow(audioId)?.data ?: ByteArray(0))
    }
}