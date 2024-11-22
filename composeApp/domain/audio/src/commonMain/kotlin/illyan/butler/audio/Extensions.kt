package illyan.butler.audio

import io.ktor.http.ContentType
import korlibs.audio.format.WAV

val Wav: ContentType
    get() = ContentType("audio", "wav")

suspend fun ByteArray.toWav() = WAV.decode(this)