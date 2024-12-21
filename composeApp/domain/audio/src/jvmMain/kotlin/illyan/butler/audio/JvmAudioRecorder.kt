package illyan.butler.audio

import io.github.aakira.napier.Napier
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.readAudioData
import korlibs.io.file.std.toVfs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import java.io.File
import java.io.IOException
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
    private var outputFile: File? = null

    override suspend fun startRecording() {
        try {
            if (targetLine != null) throw IllegalStateException("Recording is already started")
            // Specify the audio format
            val format = AudioFormat(44100f, 16, 2, true, true)

            // Get a target data line
            val info = DataLine.Info(TargetDataLine::class.java, format)
            if (!AudioSystem.isLineSupported(info)) throw IllegalStateException("Line not supported")
            targetLine = AudioSystem.getLine(info) as TargetDataLine

            // Open the target data line
            targetLine?.open(format)
            Napier.v("Target line opened: $targetLine")

            // Start capturing audio
            isRecording.update { true }
            targetLine?.start()
            Napier.v("Recording started")

            // Create a temporary file to store the captured audio
            startCapturingAudio()
        } catch (e: Exception) {
            Napier.e("Error while starting recording", e)
            targetLine?.stop()
            targetLine?.close()
            targetLine?.flush()
            targetLine = null
            isRecording.update { false }

            val mixerInfos = AudioSystem.getMixerInfo()
            var mixerInfosPrint = ""
            mixerInfos.forEach { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val lineInfos = mixer.sourceLineInfo
                lineInfos.forEach {
                    mixerInfosPrint += "${mixerInfo.name}---$it\n"
//                    val line = mixer.getLine(it)
//                    mixerInfosPrint += "\t-----${line}\n"
                }
                val targetLineInfos = mixer.targetLineInfo
                targetLineInfos.forEach {
                    mixerInfosPrint += "${mixerInfo.name}---$it\n"
//                    val line = mixer.getLine(it)
//                    mixerInfosPrint += "\t-----${line}\n"
                }
            }
            Napier.e("Mixer infos: $mixerInfosPrint")
        }
    }

    override suspend fun stopRecording(): AudioData {
        // Write captured audio data to a file
        if (targetLine == null) throw IllegalStateException("Recording is not started")
        isRecording.update { false }
        targetLine?.stop()
        targetLine?.close()
        targetLine?.flush()
        targetLine = null
        Napier.v { "Absolute path of file: ${outputFile!!.path}" }
        return outputFile!!.toVfs().readAudioData().also {
            outputFile?.delete()
            Napier.v("Deleted temporary file: $outputFile")
            Napier.v("Audio data: $it")
            outputFile = null
        }
    }

    private suspend fun startCapturingAudio() {
        val currentDir = System.getProperty("user.dir")
        Napier.v("Current directory: $currentDir")
        val prefix = "${Clock.System.now().toEpochMilliseconds()}_recording"
        Napier.v("Prefix: $prefix")
        outputFile = withContext(Dispatchers.IO) {
            File.createTempFile(prefix, ".wav", File(currentDir))
        }

        Napier.v("Output file: $outputFile")
        withContext(Dispatchers.IO) {
            try {
                Napier.v("Writing audio to file: $outputFile")
                AudioSystem.write(
                    AudioInputStream(targetLine),
                    AudioFileFormat.Type.WAVE,
                    outputFile
                )
            } catch (e: IOException) {
                Napier.e(e) { "Audio input stream closed" }
            }
        }
        Napier.v("Audio written to file: $outputFile")
    }
}