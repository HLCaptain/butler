package illyan.butler.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import io.github.aakira.napier.Napier
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.readAudioData
import korlibs.io.android.withAndroidContext
import korlibs.io.file.std.applicationVfs
import korlibs.io.file.std.toVfs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import java.io.File

@Single
class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var audioPath = ""
    override val isRecording = MutableStateFlow(recorder != null)

    override suspend fun startRecording() {
        recorder =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                audioPath = "${context.filesDir}/${Clock.System.now().toEpochMilliseconds()}_recording"
                withAndroidContext(context) {
                    File("$audioPath.3gp").createNewFile()
                }
                setOutputFile("$audioPath.3gp")
                prepare()
                start()
                isRecording.update { true }
            }
    }

    override suspend fun stopRecording(): AudioData = withContext(Dispatchers.IO) {
        if (recorder == null) throw IllegalStateException("Recording is not started")
        recorder?.apply {
            stop()
            release()
            isRecording.update { false }
        }
        recorder = null
        // Convert audio file to WAV

        val session = FFmpegKit.execute("-i $audioPath.3gp $audioPath.wav")
        if (ReturnCode.isSuccess(session.returnCode).not()) {
            Napier.e("Failed to convert audio file to WAV\n${session.output}")
        }

        File("$audioPath.wav").toVfs().readAudioData()
    }
}
