package illyan.butler.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import korlibs.audio.format.WAV
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.readAudioData
import korlibs.io.file.std.toVfs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

@Single
class AndroidAudioRecorder(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val context: Context
) : AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var audioPath = ""
    override val isRecording = MutableStateFlow(audioRecord != null)

    private val sampleRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    override suspend fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            2 * minBufferSize
        ).apply {
            var tryCount = 0
            while (state != AudioRecord.STATE_INITIALIZED && tryCount < 5) {
                delay(1.seconds)
                tryCount++
                Napier.v { "Waiting for AudioRecord to initialize" }
            }
            if (state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord failed to initialize")
            }
            startRecording()
            isRecording.update { true }
            coroutineScopeIO.launch {
                writeAudioData()
            }
        }
    }

    private fun convertPcmToWav(pcmData: ByteArray): ByteArray {
        // Implement the conversion logic from PCM to WAV format
        // This typically involves adding a WAV header to the PCM data
        // For simplicity, this example assumes 16-bit PCM, 1 channel, 44100 Hz sample rate

        val numChannels = when (channelConfig) {
            AudioFormat.CHANNEL_IN_MONO -> 1
            AudioFormat.CHANNEL_IN_STEREO -> 2
            else -> throw IllegalArgumentException("Unsupported channel configuration")
        }
        val bitsPerSample = 16
        val byteRate = sampleRateInHz * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val subchunk2Size = pcmData.size
        val chunkSize = 36 + subchunk2Size

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (chunkSize and 0xff).toByte()
        header[5] = ((chunkSize shr 8) and 0xff).toByte()
        header[6] = ((chunkSize shr 16) and 0xff).toByte()
        header[7] = ((chunkSize shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = numChannels.toByte()
        header[23] = 0
        header[24] = (sampleRateInHz and 0xff).toByte()
        header[25] = ((sampleRateInHz shr 8) and 0xff).toByte()
        header[26] = ((sampleRateInHz shr 16) and 0xff).toByte()
        header[27] = ((sampleRateInHz shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = blockAlign.toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (subchunk2Size and 0xff).toByte()
        header[41] = ((subchunk2Size shr 8) and 0xff).toByte()
        header[42] = ((subchunk2Size shr 16) and 0xff).toByte()
        header[43] = ((subchunk2Size shr 24) and 0xff).toByte()

        return header + pcmData
    }

    private suspend fun writeAudioData() = withContext(Dispatchers.IO) {
        val data = ByteArray(minBufferSize)
        val outputStream = try {
            audioPath = "${context.filesDir}/${Clock.System.now().toEpochMilliseconds()}_recording.pcm"
            FileOutputStream(audioPath)
        } catch (e: FileNotFoundException) {
            Napier.e("Could not create audio file output stream", e)
            return@withContext
        }
        while (isRecording.value) {
            val read = audioRecord!!.read(data, 0, data.size)
            try {
                outputStream.write(data, 0, read)
                // clean up file writing operations
            } catch (e: IOException) {
                Napier.e("Could not write audio data to file", e)
            }
        }
        try {
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            Napier.e("Could not close audio output stream", e)
        }
    }

    override suspend fun stopRecording(): AudioData = withContext(Dispatchers.IO) {
        if (audioRecord == null) throw IllegalStateException("Recording is not started")
        audioRecord?.apply {
            stop()
            release()
            isRecording.update { false }
        }
        audioRecord = null

        val pcmData = File(audioPath).readBytes()
        val wavData = convertPcmToWav(pcmData)
        val wavPath = audioPath.replace(".pcm", ".wav")
        File(wavPath).writeBytes(wavData)

        File(wavPath).toVfs().readAudioData(formats = WAV).also {
            File(audioPath).delete()
            Napier.v("Deleted temporary file: $audioPath")
        }
    }
}
