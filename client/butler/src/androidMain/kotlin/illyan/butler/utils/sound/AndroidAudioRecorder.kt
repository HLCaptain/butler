package illyan.butler.utils.sound

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import illyan.butler.utils.AudioRecorder
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.readAudioData
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var audioPath = ""
    override val isRecording = MutableStateFlow(recorder != null)

    override suspend fun startRecording() {
        recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            audioPath = "${Clock.System.now().toEpochMilliseconds()}_recording"
            resourcesVfs[audioPath].writeBytes(byteArrayOf())
            setOutputFile(audioPath)
            prepare()
            isRecording.update { true }
            start()
        }
    }

    override suspend fun stopRecording(): AudioData {
        if (recorder == null) throw IllegalStateException("Recording is not started")
        recorder?.apply {
            stop()
            isRecording.update { false }
            release()
        }
        recorder = null
        // Convert auido file to WAV

        val session = FFmpegKit.execute("-i $audioPath.3gp $audioPath.wav")
        if (ReturnCode.isSuccess(session.returnCode).not()) {
            throw IllegalStateException("Failed to convert audio file to WAV")
        }

        return resourcesVfs["$audioPath.wav"].readAudioData()
    }
}