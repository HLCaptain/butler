package illyan.butler.utils.sound

import illyan.butler.utils.AudioRecorder
import io.github.aakira.napier.Napier
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.readAudioData
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

@Single
class JvmAudioRecorder : AudioRecorder {
    private var targetLine: TargetDataLine? = null
    override val isRecording = MutableStateFlow(targetLine != null)

    override suspend fun startRecording() {
        // Specify the audio format
        val format = AudioFormat(44100f, 16, 2, true, true)

        // Get a target data line
        val info = DataLine.Info(TargetDataLine::class.java, format)
        targetLine = AudioSystem.getLine(info) as TargetDataLine

        // Open the target data line
        targetLine?.open(format)

        // Start capturing audio
        isRecording.update { true }
        targetLine?.start()
    }

    override suspend fun stopRecording(): AudioData {
        // Write captured audio data to a file
        if (targetLine == null) throw IllegalStateException("Recording is not started")
        targetLine?.stop()
        isRecording.update { false }
        targetLine?.close()
        val outputFile = withContext(Dispatchers.IO) {
            val currentDir = System.getProperty("user.dir")
            Napier.v("Current directory: $currentDir")
            val prefix = "${Clock.System.now().toEpochMilliseconds()}_recording"
            Napier.v("Prefix: $prefix")
            val outputFile = File.createTempFile(prefix, ".wav", File(currentDir))
            Napier.v("Output file: $outputFile")
            AudioSystem.write(AudioInputStream(targetLine), AudioFileFormat.Type.WAVE, outputFile)
            outputFile
        }
        targetLine = null
        return resourcesVfs[outputFile.absolutePath].readAudioData().also {
            outputFile.delete()
            Napier.v("Deleted temporary file: $outputFile")
        }
    }
}