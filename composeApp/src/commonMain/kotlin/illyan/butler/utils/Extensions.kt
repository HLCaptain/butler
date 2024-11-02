package illyan.butler.utils

import io.ktor.http.ContentType
import korlibs.audio.format.WAV

val ContentType.Audio.Wav: ContentType
    get() = ContentType("audio", "wav")

suspend fun ByteArray.toWav() = WAV.decode(this)
